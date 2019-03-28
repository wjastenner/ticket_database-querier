-- indexing?


DROP schema IF EXISTS pierian_games;

SET SEARCH_PATH
TO studentdb;

CREATE schema pierian_games;

SET SEARCH_PATH
TO studentdb, pierian_games;

DROP TABLE IF EXISTS event
CASCADE;
DROP TABLE IF EXISTS spectator
CASCADE;
DROP TABLE IF EXISTS ticket
CASCADE;
DROP TABLE IF EXISTS cancel
CASCADE;

CREATE TABLE event
(
  ecode CHAR(4),
  edesc VARCHAR(20) NOT NULL,
  elocation VARCHAR(20) NOT NULL,
  edate DATE NOT NULL CHECK (edate BETWEEN '2019-04-01' AND '2019-04-30'),
  etime TIME NOT NULL CHECK (etime >= TIME
  '09:00:00'),
    emax         SMALLINT NOT NULL CHECK
  (emax BETWEEN 0 AND 1000),
    CONSTRAINT event_pk_ecode PRIMARY KEY
  (ecode)
);

  CREATE TABLE spectator
  (
    sno INTEGER,
    sname VARCHAR(20) NOT NULL,
    semail VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT spectator_pk_sno PRIMARY KEY (sno)
  );

  CREATE TABLE ticket
  (
    tno INTEGER,
    ecode CHAR(4) NOT NULL,
    sno INTEGER NOT NULL,
    CONSTRAINT ticket_pk_tno PRIMARY KEY (tno),
    CONSTRAINT ticket_fk_ecode FOREIGN KEY (ecode) REFERENCES event ON DELETE CASCADE,
    CONSTRAINT ticket_fk_sno FOREIGN KEY (sno) REFERENCES spectator,
    CONSTRAINT ticket_unq_ecode_sno UNIQUE (ecode, sno)
  );

  CREATE TABLE cancel
  (
    tno INTEGER,
    ecode CHAR(4),
    sno INTEGER REFERENCES spectator(sno) ON DELETE SET NULL,
    cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cuser VARCHAR(128) DEFAULT CURRENT_USER,
    CONSTRAINT cancel_pk_tno_cdate PRIMARY KEY (tno, cdate),
    CONSTRAINT cancel_fk_sno FOREIGN KEY (sno) REFERENCES spectator ON DELETE SET NULL
  );

  CREATE OR REPLACE FUNCTION move_ticket
  ()
RETURNS TRIGGER AS
$$
  BEGIN
    INSERT INTO cancel
    VALUES(OLD.tno, OLD.ecode, OLD.sno);
    RETURN OLD;
  END;
  $$
LANGUAGE PLPGSQL;

  CREATE TRIGGER move_ticket_trigger
BEFORE
  DELETE ON ticket
FOR EACH
  ROW
  EXECUTE PROCEDURE move_ticket
  ();




  CREATE OR REPLACE FUNCTION ticket_status
  (IN INTEGER)
RETURNS VARCHAR
  (20) AS
$$
  BEGIN
    IF ((SELECT COUNT(tno)
    FROM ticket
    WHERE tno = $1) = 1)
    THEN
    RETURN 'Valid';
    ELSEIF
    ((SELECT COUNT(tno)
    FROM cancel
    WHERE tno = $1)
    = 1)
    THEN
    RETURN 'Cancelled';
    ELSE
    RETURN 'tno not recognised';
  END
  IF;
END;
$$
LANGUAGE PLPGSQL;

  CREATE VIEW tickets_all
  AS
          SELECT tno, ecode, sno, ticket_status(tno) AS ticket_status
      FROM ticket
    UNION
      SELECT tno, ecode, sno, ticket_status(tno) AS ticket_status
      FROM cancel
      ORDER BY tno;


  CREATE OR REPLACE FUNCTION insert_ticket
  (IN CHAR
  (4), IN INT)
RETURNS VOID AS
$$
  BEGIN
    IF((SELECT count(tno)
    FROM ticket
    WHERE ecode = $1) = (SELECT emax
    FROM event
    WHERE ecode = $1))
    THEN RAISE EXCEPTION 'E. There are no tickets for that event left';
  ELSE
  INSERT INTO ticket
  VALUES
    ((SELECT COALESCE(MAX(tno), 0)
      FROM tickets_all)+1, $1, $2);
  END
  IF;
  END;
$$
LANGUAGE PLPGSQL;

  CREATE OR REPLACE FUNCTION delete_spectator
  (IN INT)
RETURNS VOID AS
$$
  BEGIN
    IF ($1 IN (SELECT sno
    FROM spectator
    WHERE sno = $1))
    THEN
    DELETE FROM spectator WHERE sno = $1;
    ELSE RAISE EXCEPTION 'C. Spectator does not exist';
  END
  IF;
  END;
$$
LANGUAGE PLPGSQL;

  CREATE OR REPLACE FUNCTION delete_event
  (IN CHAR
  (4))
RETURNS VOID AS
$$
  BEGIN
    IF ($1 IN (SELECT ecode
    FROM event
    WHERE ecode = $1))
    THEN
    DELETE FROM event WHERE ecode = $1;
    ELSE RAISE EXCEPTION 'D. Event does not exist';
  END
  IF;
  END;
$$
LANGUAGE PLPGSQL;

  CREATE OR REPLACE FUNCTION travel_query
  ()
RETURNS TABLE
  (
  event_location VARCHAR
  (20),
  event_date DATE,
  total_tickets BIGINT
)
AS
$$
  BEGIN
    RETURN QUERY
    SELECT elocation, edate, count(ticket.ecode) AS tickets_issued
    FROM event
      LEFT JOIN ticket ON event.ecode = ticket.ecode
    GROUP BY elocation, edate
    ORDER BY elocation;
  END;
  $$
LANGUAGE PLPGSQL;

  CREATE OR REPLACE FUNCTION tickets_per_event
  ()
RETURNS TABLE
  (
  ecode CHAR
  (4),
  edesc VARCHAR
  (20),
  tickets_issued BIGINT
)
AS
$$
  BEGIN
    RETURN QUERY
    SELECT event.ecode, event.edesc, count(ticket.ecode) AS tickets_issued
    FROM event
      LEFT JOIN ticket ON event.ecode = ticket.ecode
    GROUP BY event.ecode
    ORDER BY edesc;
  END;
  $$
LANGUAGE PLPGSQL;


  CREATE OR REPLACE FUNCTION tickets_specific_event
  (IN CHAR
  (4))
RETURNS TABLE
  (
  eventCode CHAR
  (4),
  eventDesc VARCHAR
  (20),
  ticketsIssued BIGINT
)
AS
$$
  BEGIN
    IF $1 NOT IN (SELECT ecode
    FROM event
    WHERE ecode = $1)
    THEN RAISE EXCEPTION 'R. Tickets per event query unsuccessful. Event not found.';
  ELSE
  RETURN QUERY
  SELECT event.ecode, event.edesc, count(ticket.ecode) AS tickets_issued
  FROM event
    LEFT JOIN ticket ON event.ecode = ticket.ecode
  WHERE event.ecode = $1
  GROUP BY event.ecode;
  END
  IF;
  END;
$$
LANGUAGE PLPGSQL;

  CREATE OR REPLACE FUNCTION spectator_itinerary
  (IN INT)
RETURNS TABLE
  (
    spectatorName VARCHAR
  (20),
    eventDate DATE,
    eventLocation VARCHAR
  (20),
    eventTime TIME,
    eventDesc VARCHAR
  (20)
)
AS
$$
  BEGIN
    IF $1 NOT IN (SELECT sno
    FROM spectator
    WHERE sno = $1)
    THEN RAISE EXCEPTION 'S. Spectator itinerary unsuccessful. Spectator not found.';
  ELSE
  RETURN QUERY
  SELECT spectator.sname, edate, elocation, etime, edesc
  FROM ticket
    LEFT JOIN spectator ON ticket.sno = spectator.sno
    LEFT JOIN event ON ticket.ecode = event.ecode
  WHERE spectator.sno = $1
  ORDER BY event.edate, event.etime;
  END
  IF;
  END;
$$
LANGUAGE PLPGSQL;



  CREATE OR REPLACE FUNCTION ticket_details
  (IN INT)
RETURNS TABLE
  (
    spectatorName VARCHAR
  (20),
    eventCode CHAR
  (4),
    ticketStatus VARCHAR
  (20)
)
AS
$$
  BEGIN
    IF $1 NOT IN (SELECT tno
    FROM tickets_all
    WHERE tno = $1)
    THEN RAISE EXCEPTION 'T. Ticket details query unsuccessful. Ticket does not exist.';
  ELSE
  RETURN QUERY
  SELECT sname, ecode, ticket_status
  FROM spectator
    LEFT JOIN tickets_all
    ON spectator.sno = tickets_all.sno
  WHERE tno = $1;
  END
  IF;
  END;
$$
LANGUAGE PLPGSQL;



  CREATE OR REPLACE FUNCTION cancelled_event_ticket_details
  (IN CHAR
  (4))
RETURNS TABLE
  (
    ticketNumber INT,
    eventCode CHAR
  (4),
    spectatorNumber INT,
    cancelledDate TIMESTAMP,
    currentUser VARCHAR
  (128)
)
AS
$$
  BEGIN
    IF($1 NOT IN (SELECT DISTINCT ecode
    FROM tickets_all))
    THEN RAISE EXCEPTION 'V. Cancelled event ticket query unsuccessful. Event does not exist.';
  ELSEIF
  ($1 IN
  (SELECT ecode
  FROM event
  WHERE ecode = $1)
  )
    THEN RAISE EXCEPTION 'V. Cancelled event ticket query unsuccessful. This event has not been cancelled.';
    ELSE
  RETURN QUERY
  SELECT *
  FROM cancel
  WHERE ecode = $1;
  END
  IF;
  END;
$$
LANGUAGE PLPGSQL;
