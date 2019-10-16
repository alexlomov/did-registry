END;
CREATE DATABASE did_registry;

create table public_keys2doc(
  pubk_id varchar(1024) primary key,
  did_doc_id varchar(1024) not null
);

create table did_doc(
  doc_id varchar(1024) primary key,
  doc jsonb not null
)