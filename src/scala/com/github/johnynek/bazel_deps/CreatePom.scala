package com.github.johnynek.bazel_deps
import scala.xml._

object CreatePom {
  implicit class MavenCoordinateExtension(private val self: MavenCoordinate) extends AnyVal {
    def toXml: Elem = {
      <dependency>
        <groupId>{self.group.asString}</groupId>
        <artifactId>{self.artifact.asString}</artifactId>
        <version>{self.version.asString}</version>
      </dependency>
    }
  }

  def translate(dependencies: Graph[MavenCoordinate, Unit], model: Model): String = {
    def replaced(m: MavenCoordinate): Boolean = model.getReplacements.get(m.unversioned).isDefined
    val mavenCoordinateXml = dependencies.nodes.filterNot(replaced).toList.map {
      d => d.toXml
    }

    val pomXml = <project>
      <modelVersion>4.0.0</modelVersion>

      <dependencies>
        {mavenCoordinateXml}
      </dependencies>
    </project>

    val p = new scala.xml.PrettyPrinter(80, 2)
    p.format(pomXml)
  }

  def apply(dependencies: Graph[MavenCoordinate, Unit], model: Model, path: String): Unit = {
    scala.xml.XML.save(
      path,
      scala.xml.XML.loadString(translate(dependencies, model)),
      "UTF-8",
      true
    )
  }
}
