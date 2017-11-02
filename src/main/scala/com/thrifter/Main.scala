package com.thrifter

import java.io.{File, _}

import com.twitter.scrooge.Compiler
import com.twitter.scrooge.backend.WithFinagle
import scopt.OptionParser

import scala.tools.nsc._

object Main extends App {

    override def main(args: Array[String]): Unit = {
        val compiler = new Compiler
        compiler.language = "scala"
        compiler.flags += WithFinagle
        compiler.destFolder = "target/thrift"
        if (!parseArguments(compiler, args)) {
            System.exit(1)
        }
        compiler.run()

        compileGeneratedFiles(compiler.destFolder)
    }

    def listDirs(dir: File) =
        dir.listFiles(new FileFilter {
            override def accept(pathname: File): Boolean = pathname.isDirectory
        }).toList

    def listScalaSourceFiles(dirName: String): List[String] = listScalaSourceFiles(new File(dirName))

    def listScalaSourceFiles(dir: File): List[String] =
        dir.listFiles(new FilenameFilter {
            override def accept(dir: File, name: String): Boolean = name.endsWith(".scala")
        }).map(f => f.getCanonicalPath).toList ::: listScalaSourceFiles(listDirs(dir))

    def listScalaSourceFiles(dirs: List[File]): List[String] =
        dirs.flatMap(f => listScalaSourceFiles(f))

    def compileGeneratedFiles(destFolder: String) = {
        val s = new Settings()
        s.usejavacp.value = true
        val g = new Global(s)

        val run = new g.Run

        run.compile(listScalaSourceFiles(destFolder))

//        val classLoader = new java.net.URLClassLoader(
//            Array(new File(".").toURI.toURL),  // Using current directory.
//            this.getClass.getClassLoader)
//
//        val clazz = classLoader.loadClass("Test") // load class
//
//        clazz.newInstance
    }

    private def parseArguments(compiler: Compiler, args: Array[String]) = {
        val parser = new OptionParser[Compiler]("thrifter") {
            help("help").text("show this help screen")

            opt[Unit]('v', "verbose").action { (_, c) =>
                c.verbose = true
                c
            }.text("log verbose messages about progress")

            opt[String]('d', "dest").valueName("<path>").action { (d, c) =>
                c.destFolder = d
                c
            }.text("write generated code to a folder (default: %s)".format(compiler.defaultDestFolder))

            opt[String]('i', "include-path").unbounded().valueName("<path>").action { (path, c) =>
                c.includePaths ++= path.split(File.pathSeparator)
                c
            }.text("path(s) to search for included thrift files (may be used multiple times)")

            arg[String]("<files...>").unbounded().action { (files, c) =>
                c.thriftFiles += files
                c
            }.text("thrift files to compile")
        }
        val parsed = parser.parse(args, compiler)
        parsed.isDefined
    }
}