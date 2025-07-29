-- Stored procedure for deleting notifications older than custom days

CREATE OR REPLACE PROCEDURE BICOMP.delete_old_notifications(days IN INT)
IS
	V_SQL VARCHAR2(4000);

	CURSOR DELETABLE_NOTIFICATIONS IS
	SELECT n.* FROM notification n INNER JOIN notification_user nu
        ON n.id = nu.notification_id
    WHERE nu.tms_read IS NOT NULL
    AND TO_DATE(TO_CHAR(nu.tms_read, 'dd-mon-yy')) <= TO_DATE(TO_CHAR(SYSDATE - days, 'dd-mon-yy'))
	ORDER BY n.tms_insert ASC;
BEGIN
    FOR N IN DELETABLE_NOTIFICATIONS
	LOOP
		DBMS_OUTPUT.put_line('Found notifications with ID  [' || N.id || ']  and MESSAGE  [' || N.message || ']  and TITLE  [' || N.title || ']');
		V_SQL := 'DELETE FROM NOTIFICATION_USER WHERE NOTIFICATION_ID = ' || N.id;
		DBMS_OUTPUT.PUT_LINE(V_SQL);
		EXECUTE IMMEDIATE V_SQL;

		V_SQL := 'DELETE FROM NOTIFICATION WHERE ID = ' || N.id;
		DBMS_OUTPUT.PUT_LINE(V_SQL);
		EXECUTE IMMEDIATE V_SQL;
	END LOOP;

EXCEPTION
	WHEN OTHERS THEN
		DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;