-- Modifica nome classe Delete Files
UPDATE "BICOMP"."TIMER"
SET JOB_NAME = 'DELETE_FILES_processor', JOB_CLASS = 'it.popso.bicomp.quartz.job.DeleteFilesProcessorJob'
WHERE JOB_NAME = 'DELETE_OLD_FILES_processor';
