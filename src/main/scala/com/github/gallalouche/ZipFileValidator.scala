package com.github.gallalouche

import java.io.{File => JFile}
import java.util.zip.ZipEntry

import better.files.File
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import com.github.gallalouche.ZipFileValidator.MissingFile

trait ZipFileValidator {
  /**
   * Validates only the zip structure, not its actual content. For example, it won't check that the
   * file compiles or anything like that.
   */
  def validate(entries: Seq[ZipEntry]): ValidatedNel[MissingFile, Unit]
}

object ZipFileValidator {
  case class MissingFile(file: String)
  /**
   * strict means validating that the zipfile contains the expected files in the top level
   * directory. Unfortunately, students aren't very good at reading instructions, so you might want
   * to relax this.
   */
  def forFiles(file1: String, fileRest: String*)(strict: Boolean): ZipFileValidator =
    new FileValidator(file1 +: fileRest, strict)
  private class FileValidator(files: Seq[String], strict: Boolean) extends ZipFileValidator {
    override def validate(entries: Seq[ZipEntry]): ValidatedNel[MissingFile, Unit] = {
      val filesInDirs =
        entries.view
          .filterNot(_.isDirectory)
          .map(split.compose(_.getName))
          .groupBy(_._1)
          .view
          .mapValues(_.map(_._2).toSet)
          .toMap

      def validate(set: Set[String]) =
        NonEmptyList
          .fromList(files.view.filterNot(set.contains).map(MissingFile.apply).toList)
          .fold(Validated.validNel(()))(Validated.Invalid.apply)

      lazy val strictValidate = validate(filesInDirs.getOrElse("", Set.empty))
      if strict then strictValidate
      else if filesInDirs.values.exists(validate(_).isValid) then Validated.validNel(())
      else strictValidate
    }

    private def split(path: String): (String, String) = {
      val file = new JFile(path)
      (Option(file.getParent).getOrElse(""), file.getName)
    }
  }

  case object Noop extends ZipFileValidator {
    override def validate(entries: Seq[ZipEntry]): ValidatedNel[Nothing, Unit] =
      Validated.validNel(())
  }
}
