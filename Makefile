config ?= runtimeClasspath

ifdef module
mm = :${module}:
else
mm =
endif


compile:
	 ./gradlew assemble

check:
	./gradlew check

image:
	./gradlew jibDockerBuild


gen-api:
	./gradlew generateApiCode

gen-docs: 
	./gradlew generateSwaggerUI

#
# Show dependencies try `make deps config=runtime`, `make deps config=google`
#
deps:
	./gradlew -q ${mm}dependencies --configuration ${config}
