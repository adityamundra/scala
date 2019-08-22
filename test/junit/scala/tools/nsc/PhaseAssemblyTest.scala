/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.tools.nsc

import org.junit.Assert.assertEquals
import org.junit.Test

class PhaseAssemblyTest {
  @Test
  def multipleRunsRightAfter(): Unit = {
    val g = new Global(new Settings)
    case class component(phaseName: String, override val runsRightAfter: Option[String], override val runsAfter: List[String], override val runsBefore: List[String]) extends SubComponent {
      val global: g.type = g
      override def newPhase(prev: Phase): Phase = ???
    }

    val N = 16
    val random = new scala.util.Random(123502L)
    val names = Array.fill(N)("phase_" + random.nextInt(1024))
    val parserAndTerminal = List(
      component("parser", None, Nil, Nil),
      component("terminal", None, Nil, List(N.toString))
    )
    val components = List.tabulate(N)(i => component(names(i), Some(if (i == 0) "parser" else names(i - 1)), Nil, List("terminal"))) ::: parserAndTerminal

    val graph = g.phasesSetToDepGraph(components.reverse)
    graph.removeDanglingNodes()
    graph.collapseHardLinks()
    graph.assignLevelsAndDetectCycles(graph.getNodeByPhase("parser"))
    val result: List[String] = graph.compilerPhaseList().map(_.phaseName).filter(_.startsWith("phase_"))
    assertEquals(names.toList, result)
  }

}