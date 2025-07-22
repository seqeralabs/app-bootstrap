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


gen-code:
	./gradlew generateApiCode

#
# Show dependencies try `make deps config=runtime`, `make deps config=google`
#
deps:
	./gradlew -q ${mm}dependencies --configuration ${config}
