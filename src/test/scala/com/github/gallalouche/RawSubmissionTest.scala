package com.github.gallalouche

import better.files.{DisposeableExtensions, File}
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.{Concurrent, IO}
import cats.effect.unsafe.implicits.global
import com.github.gallalouche.RawSubmission.{nameError, ContentError, FileError, NameError}
import com.github.gallalouche.RawSubmissionTest.validate
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class RawSubmissionTest extends AnyFreeSpec with Matchers {
  "Validation" - {
    "Invalid file name" - {
      "No ids" in validateErrors("BadName.zip", nameError("BadName.zip"))
      "Too many ids" in validateErrors("123_456_789.zip", nameError("123_456_789.zip"))
    }
    "Invalid content" - {
      "Not a zip file" in {
        validateErrors(
          "1234.zip",
          ContentError("Invalid zip file: 'zip END header not found'"),
        )
      }
      "Missing files" in {
        validateErrors(
          "5678.zip",
          ContentError("Missing file: 'Expected.hs'"),
        )(ZipFileValidator.forFiles("Expected.hs")(strict = true))
      }
      "Multiple errors" in {
        validateErrors(
          "BadZip.zip",
          nameError("BadZip.zip"),
          ContentError("Missing file: 'Expected.hs'"),
        )
      }
    }
    "Valid submissions" - {
      "Single ID" in {validateSuccess("123_456.zip")}
      "Strict check" in {
        validateSuccess("123_456.zip", ZipFileValidator.forFiles("empty")(strict = true))
      }
      "Lenient check" in {
        validateSuccess("456_123.zip", ZipFileValidator.forFiles("good_file.hs")(strict = false))
      }
    }
  }

  private def validateErrors(
      fileName: String,
      e1: FileError,
      eRest: FileError*,
  )(validator: ZipFileValidator = ZipFileValidator.Noop) =
    validate(fileName, validator) shouldBe Validated.Invalid(NonEmptyList.of(e1, eRest*))

  private def validateSuccess(
    fileName: String,
    validator: ZipFileValidator = ZipFileValidator.Noop,
  ) = validate(fileName, validator) shouldBe Validated.Valid(())
}

private object RawSubmissionTest {
  private def validate(fileName: String, validator: ZipFileValidator = ZipFileValidator.Noop) = {
    val file = File(better.files.Resource.at[RawSubmissionTest].getUrl(fileName))
    RawSubmission(file).validate[IO](validator).unsafeRunSync()
  }
}
