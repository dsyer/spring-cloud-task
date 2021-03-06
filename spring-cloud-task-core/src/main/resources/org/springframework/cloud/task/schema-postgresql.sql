
CREATE TABLE TASK_EXECUTION  (
	TASK_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	START_TIME TIMESTAMP DEFAULT NULL ,
	END_TIME TIMESTAMP DEFAULT NULL ,
	TASK_NAME  VARCHAR(100) ,
	EXIT_CODE INTEGER ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP
);

CREATE TABLE TASK_EXECUTION_PARAMS  (
	TASK_EXECUTION_ID BIGINT NOT NULL ,
	TASK_PARAM VARCHAR(250) ,
	constraint TASK_EXEC_PARAMS_FK foreign key (TASK_EXECUTION_ID)
	references TASK_EXECUTION(TASK_EXECUTION_ID)
) ;

CREATE SEQUENCE TASK_SEQ MAXVALUE 9223372036854775807 NO CYCLE;