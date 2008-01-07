NAME=$1
VERSION="0.1-SNAPSHOT"

mkdir $NAME

# pom.xml
echo "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">

	<modelVersion>4.0.0</modelVersion>

	<parent>
		<groupId>se.l4.crayon</groupId>
		<artifactId>crayon</artifactId>
		<version>$VERSION</version>
	</parent>
	
	<artifactId>$NAME</artifactId>
	<packaging>jar</packaging>
	<name>$NAME</name>
  
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				
				<configuration>
					<source>1.5</source>
					<target>1.5</target>
				</configuration>
			</plugin>
		</plugins>
	</build>

	<dependencies>
		<!--
		<dependency>
			<groupId>$\{project.groupId}</groupId>
			<artifactId>crayon-project</artifactId>
			<version>$\{project.version}</version>
		</dependency>
		-->
	</dependencies>
</project>
" > $NAME/pom.xml

# folders
mkdir -p $NAME/src/main/java
mkdir -p $NAME/src/test/java

# svn
svn add $NAME
svn propset svn:ignore ".project
.classpath
.settings
target" $NAME
