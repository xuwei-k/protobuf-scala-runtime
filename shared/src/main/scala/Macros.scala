package test

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros

object Macros {
  def assertEqual(a: Any, b: Any): Unit = macro assertEqualImpl
  def assertEqual[A](a: Array[A], b: Array[A]): Unit = macro assertEqualArrayImpl

  def assertEqualArrayImpl(c: Context)(a: c.Tree, b: c.Tree): c.Tree = {
     import c.universe._
    val codeA = showCode(a)
    val codeB = showCode(b)
    val tree = q"""
    val c = $a
    val d = $b
    if(_root_.java.util.Arrays.equals(c, d) == false){
      val message = c + " [" + $codeA + "] is not equals " + d + " [" + $codeB + "]"
      throw new AssertionError(message)
    }
    """
    tree
  }

  def assertEqualImpl(c: Context)(a: c.Tree, b: c.Tree): c.Tree = {
    import c.universe._
    val codeA = showCode(a)
    val codeB = showCode(b)
    val tree = q"""
    val c = $a
    val d = $b
    if(c != d){
      val message = c + " [" + $codeA + "] is not equals " + d + " [" + $codeB + "]"
      throw new AssertionError(message)
    }
    """
    tree
  }
}
