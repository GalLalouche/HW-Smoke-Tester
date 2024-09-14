package com.github.gallalouche

import java.io.ObjectInputFilter.Status

import better.files.{File, InputStreamExtensions}
import cats.data.Validated
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.eclipse.jetty.http.HttpStatus
import org.scalatra.{AsyncResult, BadRequest, FlashMapSupport, FutureSupport, ScalatraServlet, StatusCodeRouteMatcher}
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig}

import scala.concurrent.{ExecutionContext, Future}

class MyScalatraServlet
    extends ScalatraServlet
    with FileUploadSupport
    with FutureSupport
    with FlashMapSupport {
  protected implicit override val executor: ExecutionContext = ExecutionContext.global
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(1024 * 1024)))
  error { case e: Throwable =>
    e.printStackTrace()
    "Very big bad things are happening..." + e.getMessage
  }
  post("/") {
    new AsyncResult {
      override val is =
        fileParams.get("file") match
          case Some(file) =>
            for {
              tempDir <- Future(File.newTemporaryDirectory())
              _ = tempDir.deleteOnExit()
              tempFile <- Future(File(tempDir, file.name).createFile())
              _ <- Future(tempFile.write(file.getInputStream.asString()))
              res <- RawSubmission(tempFile)
                .validate[IO](ZipFileValidator.forFiles("HW1.hs")(strict = true))
                .unsafeToFuture()
            } yield {
              println(res)
              res match
                case Validated.Valid(a) =>
                  response.status = HttpStatus.OK_200
                  "File is valid!"
                case Validated.Invalid(e) =>
                  response.status = HttpStatus.BAD_REQUEST_400
                  "Submission is not OK!\n" + e.map(_.message).toList.mkString("\n")
            }
          case None => Future(BadRequest("You didn't upload a file!"))
    }
  }
}
