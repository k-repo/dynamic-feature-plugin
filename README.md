# dynamic-feature-plugin


To answer the STD requirements, the maven dynamic-feature-plugin operate in two phases : 

##Phase 1 - Feature project

- Create a feature project
- Link the new project feature to the assembly
- Add a custom component in the dependency section
- Add the dynamic-feature-plugin in the plugins section


	               <plugin>
				<groupId>com.soprahr.foryou.hub.tools.mavenplugin</groupId>
				<artifactId>dynamic-feature-plugin</artifactId>
				<version>1.0-SNAPSHOT</version>
				<executions>
					<execution>
						<goals>
							<goal>feature-dynamic-adapter</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<scope>test</scope>
				</configuration>
			</plugin>


In the clean phase the plugin will generate a .cfg file and store it in src/main/resources before the packaging.


##Phase 2 - Assembly project
- Adding the generated .cfg file in the assembly ressources
- Add the dynamic-feature-plugin in the plugins section

               <plugin>
				<groupId>com.soprahr.foryou.hub.tools.mavenplugin</groupId>
				<artifactId>dynamic-feature-plugin</artifactId>
				<version>1.0-SNAPSHOT</version>
				<executions>
					<execution>
						<goals>
							<goal>feature-dynamic-resolver</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<scope>test</scope>
				</configuration>
			</plugin>
			
			
The maven plugin will calculate the missing informations and rewrite the .cfg file in the clean phase before storing it in src/main/resources.

##Phase 3 - Bundle monitor
The bundle monitor have to optionally read the new generated .cfg file and apply its statments.

	