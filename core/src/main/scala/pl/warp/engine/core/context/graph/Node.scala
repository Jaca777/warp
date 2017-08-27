package pl.warp.engine.core.context.graph

/**
  * @author Jaca777
  *         Created 2015-12-23 at 11
  */
case class Node[+V](value: V, connections: Node[V]*) {

  def checkForCycle(): Unit = {
    def checkForCycle(toVisit: Node[V], visited: List[V]): Unit = {
      if (visited.contains(toVisit.value)) {
        throw CycleFoundException(visited :+ toVisit.value)
      } else {
        val path = visited :+ toVisit.value
        toVisit.connections.foreach(leaf => checkForCycle(leaf, path))
      }
    }
    checkForCycle(this, List.empty)
  }

  def accept[A >: V](visitor: GraphVisitor[A]): Unit = {
    for (node <- connections) node.accept(visitor)
    visitor.visitNode(this)
  }


}