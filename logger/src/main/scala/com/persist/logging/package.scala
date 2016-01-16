package com.persist

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

package object logging {

  type RichMsg = Any

  implicit def sourceLocation: () => SourceLocation = macro sourceLocationMacro

  def sourceLocationMacro(c: Context): c.Expr[() => SourceLocation] = {
    import c.universe._

    val p = c.macroApplication.pos
    val file = p.source.file.name
    val line = p.line

    def allOwners(s: c.Symbol): Seq[c.Symbol] = {
      if (s == `NoSymbol`) {
        Seq()
      } else {
        s +: allOwners(s.owner)
      }
    }
    val owners = allOwners(c.internal.enclosingOwner)

    val className = owners
      .filter(s => s.toString.startsWith("class") || s.toString.startsWith("object"))
      .map(s => s.asClass.name.toString())
      .reverse
      .mkString("$")
    val packageName = owners
      .filter(_.isPackage)
      .map(_.name.toString())
      .filter(_ != "<root>")
      .reverse
      .mkString(".")

    c.Expr[() => SourceLocation](q"() => SourceLocation($file,$packageName,$className,$line)")
  }
}
