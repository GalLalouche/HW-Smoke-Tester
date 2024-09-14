package com.github.gallalouche

import java.util.zip.ZipFile

import better.files.File
import cats.data.{Validated, ValidatedNel}
import cats.effect.kernel.Async
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxApplyOps, toFlatMapOps, toFunctorOps}
import com.github.gallalouche.RawSubmission.{nameError, ContentError, FileError, NameError, SubmissionRegex}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.StreamConverters.StreamHasToScala
import scala.util.{Failure, Success, Try}

class RawSubmission(file: File) {
  def validate[F[_]: Async](zfv: ZipFileValidator): F[ValidatedNel[FileError, Unit]] = for {
    v1 <- validateFileName.pure
    v2 <- validateZipFile(zfv)
  } yield v1 *> v2
  private def validateFileName: ValidatedNel[FileError, Unit] = {
    val name = file.name
    Validated.condNel(SubmissionRegex.matches(file.name), (), nameError(name))
  }
  private def validateZipFile[F[_]](
      zfv: ZipFileValidator,
  )(using async: Async[F]): F[ValidatedNel[FileError, Unit]] = for {
    ec <- async.executionContext
    zipFileTry <- async.fromFuture(Future(Try(new ZipFile(file.toJava)))(ec).pure)
  } yield zipFileTry match
    case Failure(e) => Validated.invalidNel(ContentError(s"Invalid zip file: '${e.getMessage}'"))
    case Success(zf) =>
      zfv
        .validate(zf.stream.toScala(Vector))
        .leftMap(_.map(f => ContentError(s"Missing file: '${f.file}'")))
}

object RawSubmission {
  private val SubmissionRegex = """\d+(_\d+)?.zip""".r
  sealed abstract class FileError(val message: String)
  final case class NameError(private val s: String) extends FileError(s)
  def nameError(fileName: String): NameError =
    NameError(s"Invalid filename: '$fileName'; file should be in the format of id1[_id2].zip")
  /**
   * This is usually a fatal error, i.e., there's no reason to continue check the submission if
   * there are any errors of this kind.
   */
  final case class ContentError(private val s: String) extends FileError(s)
}
