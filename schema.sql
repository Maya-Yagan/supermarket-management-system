
    alter table Attendance 
       drop constraint FK62ob7qnlijnrbtrlfve2ef7yo;

    alter table CashBox 
       drop constraint FKcfwk0njgmo5x8kjugbw64;

    alter table CashBox 
       drop constraint FKpjx8iqvdm3jhficvfqaqudjkk;

    alter table Financial_Record 
       drop constraint FKafhlbj276c3rj35e8pj8emvop;

    alter table Financial_Record 
       drop constraint FKs37ioi2lcl4tqk21nr5lec8ct;

    alter table Notification 
       drop constraint FKr3s2f02127o3gf8m9iymviiau;

    alter table Order_Product 
       drop constraint FK4s5f7kxugg17ur7orkfwwarhx;

    alter table Order_Product 
       drop constraint FKba5ybls1t3pcono6tktua36tg;

    alter table Orders 
       drop constraint FKawcwiewbwb20ceyog859pbb3l;

    alter table Orders 
       drop constraint FKkgnl77uxq20bmh53f3pf1b84b;

    alter table Product 
       drop constraint FKexqqeaksnmmku5py194ywp140;

    alter table Product_Warehouse 
       drop constraint FKq3gxcmt4tpe3qoeq0sae5nq4l;

    alter table Product_Warehouse 
       drop constraint FKn1fh6abgea0gfrq5w8yojwypy;

    alter table Receipt 
       drop constraint FK89d9l095c21robmf290m02ac5;

    alter table Receipt 
       drop constraint FK316st8bss9qxmofi1xa5819fl;

    alter table Receipt_Item 
       drop constraint FKgd71vxn67cb230jf0ei7r87tu;

    alter table Receipt_Item 
       drop constraint FKhk6re2ra09adkl1j5lxcu83w9;

    alter table Supplier_Product 
       drop constraint FKcme30cesoh82g3ws7mbp53fb2;

    alter table Supplier_Product 
       drop constraint FKbeh2uvqgtl4axb731328ijq5b;

    alter table user_role 
       drop constraint FK7ir6hi5jr98lclgjgbyxafneu;

    alter table user_role 
       drop constraint FK11r1ghbssgkcac3bxi915twn6;

    drop table Attendance;

    drop table CashBox;

    drop table Category;

    drop table Financial_Record;

    drop table Notification;

    drop table Order_Product;

    drop table Orders;

    drop table Product;

    drop table Product_Warehouse;

    drop table Receipt;

    drop table Receipt_Item;

    drop table Role;

    drop table Settings;

    drop table Supplier;

    drop table Supplier_Product;

    drop table user_role;

    drop table Users;

    drop table Warehouse;

    create table Attendance (
       id numeric(19,0) identity not null,
        absent bit not null,
        check_in_time TIME,
        check_out_time TIME,
        attendance_date datetime not null,
        notes varchar(255),
        overtime_hours numeric(19,0),
        user_id int not null,
        primary key (id)
    );

    create table CashBox (
       id numeric(19,0) identity not null,
        closedAt datetime,
        openedAt datetime,
        status varchar(255) not null,
        totalBalance numeric(12,2) not null,
        closed_by int,
        opened_by int not null,
        primary key (id)
    );

    create table Category (
       id int identity not null,
        name varchar(255),
        primary key (id)
    );

    create table Financial_Record (
       id numeric(19,0) identity not null,
        amount numeric(12,2) not null,
        dateTime datetime,
        description varchar(255),
        type varchar(255),
        cash_box_id numeric(19,0) not null,
        issued_by int not null,
        primary key (id)
    );

    create table Notification (
       id int identity not null,
        date datetime,
        message varchar(255),
        sender_id int,
        primary key (id)
    );

    create table Order_Product (
       id int identity not null,
        amount int,
        order_id int not null,
        product_id int not null,
        primary key (id)
    );

    create table Orders (
       id int identity not null,
        delivery_date datetime,
        name varchar(255),
        order_date datetime,
        supplier_id int not null,
        user_id int,
        primary key (id)
    );

    create table Product (
       id int identity not null,
        barcode varchar(255),
        discount float,
        expirationDate datetime,
        minLimit int,
        name varchar(255),
        price float,
        productionDate datetime,
        taxPercentage float,
        unit varchar(255),
        category_id int not null,
        primary key (id)
    );

    create table Product_Warehouse (
       id int identity not null,
        amount int,
        product_id int not null,
        warehouse_id int not null,
        primary key (id)
    );

    create table Receipt (
       id numeric(19,0) identity not null,
        changeGiven numeric(12,2) not null,
        code varchar(30) not null,
        dateTime datetime not null,
        paidAmount numeric(12,2) not null,
        paymentMethod varchar(10) not null,
        status varchar(12) not null,
        totalCost numeric(19,2),
        cash_box_id numeric(19,0),
        cashier_id int not null,
        primary key (id)
    );

    create table Receipt_Item (
       id numeric(19,0) identity not null,
        discount numeric(5,2),
        lineTotal numeric(12,2) not null,
        productName varchar(120) not null,
        quantity double precision not null,
        unitPrice numeric(12,2) not null,
        product_id int not null,
        receipt_id numeric(19,0) not null,
        primary key (id)
    );

    create table Role (
       id int identity not null,
        name varchar(255),
        privilegeLevel int,
        primary key (id)
    );

    create table Settings (
       id int identity not null,
        address varchar(255),
        marketName varchar(255),
        moneyUnit varchar(255),
        phone varchar(255),
        primary key (id)
    );

    create table Supplier (
       id int identity not null,
        email varchar(255),
        name varchar(255),
        phoneNumber varchar(255),
        primary key (id)
    );

    create table Supplier_Product (
       id int identity not null,
        price float,
        product_id int not null,
        supplier_id int not null,
        primary key (id)
    );

    create table user_role (
       user_id int not null,
        role_id int not null,
        primary key (user_id, role_id)
    );

    create table Users (
       id int identity not null,
        birthDate datetime,
        email varchar(255),
        firstName varchar(255),
        gender varchar(255),
        isFullTime bit,
        isPartTime bit,
        lastName varchar(255),
        password varchar(255),
        phoneNumber varchar(255),
        salary float,
        startDate datetime,
        tcNumber varchar(255),
        work_hours int,
        primary key (id)
    );

    create table Warehouse (
       id int identity not null,
        capacity int,
        name varchar(255),
        primary key (id)
    );

    alter table Product 
       add constraint UK_a8slcwuk6kcnegxoe8hbrs9on unique (barcode);

    alter table Receipt 
       add constraint UK_8ukmk60evy82kuk349ejrdolx unique (code);

    alter table Attendance 
       add constraint FK62ob7qnlijnrbtrlfve2ef7yo 
       foreign key (user_id) 
       references Users;

    alter table CashBox 
       add constraint FKcfwk0njgmo5x8kjugbw64 
       foreign key (closed_by) 
       references Users;

    alter table CashBox 
       add constraint FKpjx8iqvdm3jhficvfqaqudjkk 
       foreign key (opened_by) 
       references Users;

    alter table Financial_Record 
       add constraint FKafhlbj276c3rj35e8pj8emvop 
       foreign key (cash_box_id) 
       references CashBox;

    alter table Financial_Record 
       add constraint FKs37ioi2lcl4tqk21nr5lec8ct 
       foreign key (issued_by) 
       references Users;

    alter table Notification 
       add constraint FKr3s2f02127o3gf8m9iymviiau 
       foreign key (sender_id) 
       references Users;

    alter table Order_Product 
       add constraint FK4s5f7kxugg17ur7orkfwwarhx 
       foreign key (order_id) 
       references Orders;

    alter table Order_Product 
       add constraint FKba5ybls1t3pcono6tktua36tg 
       foreign key (product_id) 
       references Product;

    alter table Orders 
       add constraint FKawcwiewbwb20ceyog859pbb3l 
       foreign key (supplier_id) 
       references Supplier;

    alter table Orders 
       add constraint FKkgnl77uxq20bmh53f3pf1b84b 
       foreign key (user_id) 
       references Users;

    alter table Product 
       add constraint FKexqqeaksnmmku5py194ywp140 
       foreign key (category_id) 
       references Category;

    alter table Product_Warehouse 
       add constraint FKq3gxcmt4tpe3qoeq0sae5nq4l 
       foreign key (product_id) 
       references Product;

    alter table Product_Warehouse 
       add constraint FKn1fh6abgea0gfrq5w8yojwypy 
       foreign key (warehouse_id) 
       references Warehouse;

    alter table Receipt 
       add constraint FK89d9l095c21robmf290m02ac5 
       foreign key (cash_box_id) 
       references CashBox;

    alter table Receipt 
       add constraint FK316st8bss9qxmofi1xa5819fl 
       foreign key (cashier_id) 
       references Users;

    alter table Receipt_Item 
       add constraint FKgd71vxn67cb230jf0ei7r87tu 
       foreign key (product_id) 
       references Product;

    alter table Receipt_Item 
       add constraint FKhk6re2ra09adkl1j5lxcu83w9 
       foreign key (receipt_id) 
       references Receipt;

    alter table Supplier_Product 
       add constraint FKcme30cesoh82g3ws7mbp53fb2 
       foreign key (product_id) 
       references Product;

    alter table Supplier_Product 
       add constraint FKbeh2uvqgtl4axb731328ijq5b 
       foreign key (supplier_id) 
       references Supplier;

    alter table user_role 
       add constraint FK7ir6hi5jr98lclgjgbyxafneu 
       foreign key (role_id) 
       references Role;

    alter table user_role 
       add constraint FK11r1ghbssgkcac3bxi915twn6 
       foreign key (user_id) 
       references Users;
