-- Task A: Insert a new spectator.

-- Coalesce and max(sno) have been used to automatically increment sno.

INSERT INTO spectator VALUES ((SELECT COALESCE(MAX(sno), 0) FROM ticket)+1, 'KARLA RODWELL', 'KRODDERS@HOTMAIL.COM');

-- Task B: Insert a new event.

INSERT INTO event VALUES ('LJ01', 'LONG JUMP', 'LIVERPOOL STADIUM', '2019-04-26', '13:30:00', 500);

-- Task C: Delete a spectator. The spectator must not have any valid tickets.

-- Delete spectator where sno = 1 does not work because they have valid tickets.
-- Referential integrity is applied using a foreign key.

SELECT delete_spectator(1);

-- Delete spectator where sno = 21 works because they have no valid tickets.
-- If you delete a spectator who has cancelled tickets those tickets.sno will be set to null.

SELECT delete_spectator(21);

-- Task D: Delete an event. All the tickets for the event must be cancelled.

-- Delete event where ecode = 'R100' (ticket.ecode is set to delete on cascade
-- but before it is deleted a trigger is fired and the move_ticket function is run.
-- This function moves the ticket to the cancelled table.

DELETE FROM event WHERE ecode = 'R100';

-- Task E: Issue a ticket for an event. A spectator may have only one ticket for a given event.

-- A unique constraint on ecode and sno together has been set to prevent spectators
-- from having more than one ticket per event. The insert below will not work because
-- ecode and sno together are not unique.
-- Coalesce and max(tno) have been used to automatically increment tno.

SELECT insert_ticket('R100', 2);

-- new ticket will insert because tno is unique and ecode and sno together are unique.

SELECT insert_ticket('LJ01', 2);

-- Task P: Produce a table showing the total number of spectators liable to travel
-- to a location. The table should show the total number of spectators that could
-- travel to a location on each date an event is held at a location.

-- By using a left join on event you can provide information on events whereby there
-- are no spectators expected. For example the new event ('LJ01') inserted in Task B
-- has no tickets.

SELECT * FROM travel_query();

-- Task Q: Produce a table showing the total number of tickets issued for each event.
-- Present the data in event description sequence

SELECT * FROM tickets_per_event();

-- Task R: As Q above but only for a given event which is specified by the event code

SELECT * FROM tickets_specific_event('A100');

-- Task S: Produce a table showing the itinerary for a given spectator. The spectator
-- is specified by his/her spectator number. The itinerary should contain the spectator's
-- name and the date, location, time and event description of each event for which
-- the spectator has been issued a ticket.

SELECT * FROM spectator_itinerary(9);

-- Task T: Given a specific ticket reference number, display the name of the spectator
-- and the event code for the ticket and indicate if the ticket is valid or is cancelled.

-- Create a function called ticket_status that takes in an integer (tno) and returns
-- whether it is valid or cancelled.
-- Create a view combining the common information from the ticket and cancel table
-- (tno, ecode and sno) using union and add a new column (ticket_status) that runs
-- the ticket_status function.
-- Use a select statement joining the spectators table and tickets_all view and
-- display sname, ecode and ticket_status for any given ticket number

SELECT * FROM ticket_details(5);

-- Task V: View the details of all cancelled tickets for a specific event.

SELECT * FROM cancelled_event_ticket_details('R100');

SELECT * FROM cancelled_event_ticket_details('R200');
