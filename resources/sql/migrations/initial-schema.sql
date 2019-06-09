create table LOGS (
   id        int identity(1, 1) primary key not null
  ,instant   datetime not null
  ,level     varchar(32) not null
  ,namespace varchar(1000)
  ,file      varchar(100)
  ,line      int
  ,msg       varchar(4096) not null
);

create table ACCOUNTS (
    account_id int identity(1, 1) primary key not null
    ,name varchar not null
    ,description varchar
    ,user varchar
    ,pass varchar
    ,FOREIGN KEY (account_id) REFERENCES ACCOUNTS(account_id)
);

create table BANKS (
    account_id int primary key
    ,bankaccount_id varchar not null
    ,routing varchar
    ,client_id varchar
    ,FOREIGN KEY (account_id) REFERENCES ACCOUNTS(account_id)
);

create table OAUTH (
    account_id int primary key
    ,attributes varchar
    ,FOREIGN KEY (account_id) REFERENCES ACCOUNTS(account_id)
)