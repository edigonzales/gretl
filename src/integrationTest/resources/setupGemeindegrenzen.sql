CREATE SCHEMA IF NOT EXISTS agi_hoheitsgrenzen_pub_test;

CREATE TABLE IF NOT EXISTS agi_hoheitsgrenzen_pub_test.hoheitsgrenzen_gemeindegrenze (
	t_id bigserial,
	t_ili_tid uuid NULL DEFAULT uuid_generate_v4(),
	gemeindename varchar(255) NOT NULL,
	bfs_gemeindenummer int4 NOT NULL,
	bezirksname varchar(255) NOT NULL,
	kantonsname varchar(255) NOT NULL,
	geometrie geometry (MultiPolygon, 2056),	
	CONSTRAINT hoheitsgrenzen_gemeindegrenze_pkey PRIMARY KEY (t_id),
	CONSTRAINT hoheitsgrenzen_gemndgrnze_bfs_gemeindenummer_check CHECK (((bfs_gemeindenummer >= 1) AND (bfs_gemeindenummer <= 9999)))
)
WITH (
	OIDS=FALSE
) ;

CREATE INDEX hohitsegrenzen_gemendgrnze_geometrie_idx ON agi_hoheitsgrenzen_pub_test.hoheitsgrenzen_gemeindegrenze USING GIST (geometrie);
