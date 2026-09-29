-- =========================================================================
-- MCBD GLASS MESSENGER - COMPLETE CLEAN RESTART & DATABASE SCHEMA
-- Dashboard -> cvppveogubeudebsmazd -> SQL Editor -> New Query -> Paste & Run
-- =========================================================================

-- Enable UUID extension
create extension if not exists "uuid-ossp";

-- -------------------------------------------------------------------------
-- 1. COMPLETE DATABASE WIPE & RESET (CLEAN RESTART)
-- -------------------------------------------------------------------------
drop table if exists public.messages cascade;
drop table if exists public.channels cascade;
drop table if exists public.profiles cascade;
drop table if exists public.app_versions cascade;

-- -------------------------------------------------------------------------
-- 2. CREATE PROFILES TABLE (REAL AUTHENTICATED USERS ONLY - NO DUMMY USERS)
-- -------------------------------------------------------------------------
create table public.profiles (
    id uuid primary key,
    email text,
    full_name text not null default 'Glass Member',
    avatar_url text,
    rank text default 'Glass Member',
    status text default 'online',          -- 'online', 'away', 'busy', 'offline'
    bio text default 'Active Community Member',
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

alter table public.profiles enable row level security;

create policy "Allow all users to read profiles"
    on public.profiles for select
    using (true);

create policy "Allow users to insert or update their profile"
    on public.profiles for all
    using (true)
    with check (true);

-- -------------------------------------------------------------------------
-- 3. CREATE CHANNELS TABLE (ICON IDENTIFIERS - NO EMOJIS)
-- -------------------------------------------------------------------------
create table public.channels (
    id text primary key,
    name text not null,
    topic text,
    icon text not null default 'chat',     -- Pure icon identifier
    is_announcement boolean default false,
    created_at timestamptz default now()
);

alter table public.channels enable row level security;

create policy "Allow all users to read channels"
    on public.channels for select
    using (true);

create policy "Allow insert and update on channels"
    on public.channels for all
    using (true)
    with check (true);

create policy "Allow delete on channels"
    on public.channels for delete
    using (true);

-- -------------------------------------------------------------------------
-- 4. CREATE MESSAGES TABLE (STREAMING CHAT & MEDIA GALLERIES)
-- -------------------------------------------------------------------------
create table public.messages (
    id uuid primary key default gen_random_uuid(),
    channel_id text not null,
    user_id uuid,
    user_name text not null default 'Anonymous',
    user_avatar text,
    content text not null,
    image_url text,
    secondary_image_url text,
    audio_duration text,
    reply_to_id uuid,
    reply_to_content text,
    reply_to_sender text,
    reactions text default '',             -- Formatted icon reactions (e.g. "heart:2,fire:1")
    created_at timestamptz default now()
);

create index if not exists idx_messages_channel_created on public.messages (channel_id, created_at asc);

alter table public.messages enable row level security;

create policy "Allow all users to read messages"
    on public.messages for select
    using (true);

create policy "Allow users to send messages"
    on public.messages for insert
    with check (true);

create policy "Allow users to update reactions on messages"
    on public.messages for update
    using (true)
    with check (true);

create policy "Allow users to delete messages"
    on public.messages for delete
    using (true);

-- -------------------------------------------------------------------------
-- 5. CREATE APP VERSIONS TABLE (STRICT FORCED IN-APP UPDATE SYSTEM)
-- -------------------------------------------------------------------------
create table public.app_versions (
    id text primary key default 'latest',
    version_code int not null default 1,
    version_name text not null default '1.0.0',
    min_supported_version int not null default 1,
    apk_url text not null default 'https://github.com/mcbd-johanliebert/MCBD-Management/releases/latest/download/MCBD-Glass-Messenger-v1.0.0.apk',
    github_url text default 'https://github.com/mcbd-johanliebert/MCBD-Management',
    release_notes text default 'World class UI enhancements, bug fixes, and performance improvements.',
    is_mandatory boolean not null default true,
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

alter table public.app_versions enable row level security;

create policy "Allow all users to read app_versions"
    on public.app_versions for select
    using (true);

create policy "Allow update app_versions"
    on public.app_versions for all
    using (true)
    with check (true);

-- -------------------------------------------------------------------------
-- 6. ENABLE SUPABASE REALTIME REPLICATION FOR ALL TABLES
-- -------------------------------------------------------------------------
do $$
begin
  if not exists (
    select 1 from pg_publication_tables 
    where pubname = 'supabase_realtime' and tablename = 'messages'
  ) then
    alter publication supabase_realtime add table public.messages;
  end if;

  if not exists (
    select 1 from pg_publication_tables 
    where pubname = 'supabase_realtime' and tablename = 'channels'
  ) then
    alter publication supabase_realtime add table public.channels;
  end if;

  if not exists (
    select 1 from pg_publication_tables 
    where pubname = 'supabase_realtime' and tablename = 'profiles'
  ) then
    alter publication supabase_realtime add table public.profiles;
  end if;

  if not exists (
    select 1 from pg_publication_tables 
    where pubname = 'supabase_realtime' and tablename = 'app_versions'
  ) then
    alter publication supabase_realtime add table public.app_versions;
  end if;
end $$;

-- -------------------------------------------------------------------------
-- 7. SEED INITIAL CHANNELS (PURE ICON IDENTIFIERS - NO EMOJIS)
-- -------------------------------------------------------------------------
insert into public.channels (id, name, topic, icon, is_announcement) values
    ('peak-seekers', 'peak seekers', 'Outdoor adventures, hiking & photography', 'explore', false),
    ('code-cocktails', 'code & cocktails', 'Developers, design & tech discussions', 'code', false),
    ('room-chat', 'room chat', 'Live group voice & video hangout', 'videocam', false),
    ('announcements', 'official broadcasts', 'Community updates and releases', 'campaign', true)
on conflict (id) do update set
    name = excluded.name,
    topic = excluded.topic,
    icon = excluded.icon,
    is_announcement = excluded.is_announcement;

-- -------------------------------------------------------------------------
-- 8. SEED INITIAL VERSION CONTROL RECORD (v1.0.0)
-- -------------------------------------------------------------------------
insert into public.app_versions (id, version_code, version_name, min_supported_version, apk_url, github_url, release_notes, is_mandatory)
values (
    'latest',
    1,
    '1.0.0',
    1,
    'https://github.com/mcbd-johanliebert/MCBD-Management/releases/latest/download/MCBD-Glass-Messenger-v1.0.0.apk',
    'https://github.com/mcbd-johanliebert/MCBD-Management',
    'Initial Release with World-Class Glassmorphic UI & In-App Direct Updater',
    true
)
on conflict (id) do update set
    version_code = excluded.version_code,
    version_name = excluded.version_name,
    min_supported_version = excluded.min_supported_version,
    apk_url = excluded.apk_url,
    github_url = excluded.github_url,
    release_notes = excluded.release_notes,
    is_mandatory = excluded.is_mandatory;
