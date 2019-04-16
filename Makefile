deps:
	gradle lib

server:
	gradle run

prod:
	CONFIG_NAME=application-prod gradle run
