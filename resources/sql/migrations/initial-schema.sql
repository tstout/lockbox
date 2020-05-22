create table LOGS (
   id        int identity(1, 1) primary key not null
  ,instant   datetime not null
  ,level     varchar(32) not null
  ,namespace varchar(1000)
  ,file      varchar(100)
  ,line      int
  ,msg       varchar(4096) not null
);

create sequence ACCOUNTS_SEQ start with 1 increment by 1;
create sequence TAGS_SEQ start with 1 increment by 1;


create table ACCOUNTS (
    account_id int primary key not null
    ,name varchar(256) not null
    ,description varchar(1000)
    ,user varchar(256)
    ,pass varchar(256)
);

create table BANKS (
    account_id int primary key
    ,bankaccount_id varchar(256) not null
    ,routing varchar(256)
    ,client_id varchar(256)
    ,FOREIGN KEY (account_id) REFERENCES ACCOUNTS(account_id)
    ON DELETE CASCADE
);

-- oauth info varies - stored as EDN blob
create table OAUTH (
    account_id int primary key
    ,attributes varchar(4096)
    ,FOREIGN KEY (account_id) REFERENCES ACCOUNTS(account_id)
    ON DELETE CASCADE
);

create table TAGS (
    tag_id int identity(1, 1) primary key not null
    ,name varchar(256)
    ,description varchar(4096)
);

create table ACCOUNT_TAGS(
    tag_id int
    ,account_id int
    ,FOREIGN KEY (account_id) REFERENCES ACCOUNTS(account_id)
    ON DELETE CASCADE
    ,FOREIGN KEY (tag_id) REFERENCES TAGS(tag_id)
    ON DELETE CASCADE
);