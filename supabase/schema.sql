-- One-time schema setup: run once in the Supabase SQL editor before seed.sql.
-- See specs/004-remote-lesson-content/contracts/supabase-schema.md for the full contract.

create table public.lessons (
  id         bigint primary key,
  subject    text not null,
  title      text not null,
  teaser     text not null,
  minutes    integer not null,
  has_audio  boolean not null default true,
  is_locked  boolean not null default false,
  image      text not null
);

create table public.lesson_content (
  lesson_id  bigint primary key references public.lessons(id) on delete cascade,
  author     text not null,
  body       text not null
);

alter table public.lessons enable row level security;
alter table public.lesson_content enable row level security;

create policy "Public read access" on public.lessons
  for select using (true);

create policy "Public read access" on public.lesson_content
  for select using (true);
