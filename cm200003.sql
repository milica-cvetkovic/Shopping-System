DROP DATABASE IF EXISTS cm200003
go

CREATE DATABASE cm200003
go

USE cm200003
go

CREATE TABLE [Article]
( 
	[articleId]          integer  IDENTITY  NOT NULL ,
	[articleName]        varchar(100)  NOT NULL ,
	[articlePrice]       decimal(10,3)  NOT NULL 
)
go

CREATE TABLE [ArticlesInOrder]
( 
	[count]              integer  NOT NULL ,
	[orderId]            integer  NOT NULL ,
	[articleId]          integer  NOT NULL 
)
go

CREATE TABLE [Buyer]
( 
	[buyerId]            integer  IDENTITY  NOT NULL ,
	[name]               varchar(100)  NOT NULL ,
	[cityId]             integer  NOT NULL ,
	[credit]             decimal(10,3)  NOT NULL 
)
go

CREATE TABLE [City]
( 
	[cityId]             integer  IDENTITY  NOT NULL ,
	[cityName]           varchar(100)  NOT NULL 
)
go

CREATE TABLE [Line]
( 
	[cityId1]            integer  NOT NULL ,
	[cityId2]            integer  NOT NULL ,
	[distance]           integer  NOT NULL 
)
go

CREATE TABLE [OrderDone]
( 
	[orderId]            integer  IDENTITY  NOT NULL ,
	[state]              varchar(100)  NOT NULL ,
	[buyerId]            integer  NOT NULL ,
	[sentTime]           datetime  NULL ,
	[receivedTime]       datetime  NULL ,
	[assembled]          integer  NULL ,
	[waitAssemble]       integer  NULL ,
	[currentCity]        integer  NULL 
)
go

CREATE TABLE [Sells]
( 
	[shopId]             integer  NOT NULL ,
	[articleId]          integer  NOT NULL ,
	[articleCount]       integer  NOT NULL 
)
go

CREATE TABLE [Shop]
( 
	[shopId]             integer  IDENTITY  NOT NULL ,
	[name]               varchar(100)  NOT NULL ,
	[cityId]             integer  NOT NULL ,
	[discount]           integer  NOT NULL 
)
go

CREATE TABLE [TransactionDone]
( 
	[transactionId]      integer  IDENTITY  NOT NULL ,
	[orderId]            integer  NOT NULL ,
	[dateDone]           datetime  NOT NULL ,
	[amount]             decimal(10,3)  NOT NULL 
)
go

CREATE TABLE [TransactionDoneBuyer]
( 
	[transactionId]      integer  NOT NULL ,
	[buyerId]            integer  NOT NULL 
)
go

CREATE TABLE [TransactionDoneShop]
( 
	[transactionId]      integer  NOT NULL ,
	[shopId]             integer  NOT NULL 
)
go

ALTER TABLE [Article]
	ADD CONSTRAINT [XPKArticle] PRIMARY KEY  CLUSTERED ([articleId] ASC)
go

ALTER TABLE [ArticlesInOrder]
	ADD CONSTRAINT [XPKArticlesInOrder] PRIMARY KEY  CLUSTERED ([orderId] ASC,[articleId] ASC)
go

ALTER TABLE [Buyer]
	ADD CONSTRAINT [XPKBuyer] PRIMARY KEY  CLUSTERED ([buyerId] ASC)
go

ALTER TABLE [City]
	ADD CONSTRAINT [XPKCity] PRIMARY KEY  CLUSTERED ([cityId] ASC)
go

ALTER TABLE [Line]
	ADD CONSTRAINT [XPKLine] PRIMARY KEY  CLUSTERED ([cityId1] ASC,[cityId2] ASC)
go

ALTER TABLE [OrderDone]
	ADD CONSTRAINT [XPKOrderDone] PRIMARY KEY  CLUSTERED ([orderId] ASC)
go

ALTER TABLE [Sells]
	ADD CONSTRAINT [XPKSells] PRIMARY KEY  CLUSTERED ([shopId] ASC,[articleId] ASC)
go

ALTER TABLE [Shop]
	ADD CONSTRAINT [XPKShop] PRIMARY KEY  CLUSTERED ([shopId] ASC)
go

ALTER TABLE [TransactionDone]
	ADD CONSTRAINT [XPKTransactionDone] PRIMARY KEY  CLUSTERED ([transactionId] ASC)
go

ALTER TABLE [TransactionDoneBuyer]
	ADD CONSTRAINT [XPKTransactionDoneBuyer] PRIMARY KEY  CLUSTERED ([transactionId] ASC)
go

ALTER TABLE [TransactionDoneShop]
	ADD CONSTRAINT [XPKTransactionDoneShop] PRIMARY KEY  CLUSTERED ([transactionId] ASC)
go


ALTER TABLE [ArticlesInOrder]
	ADD CONSTRAINT [R_1] FOREIGN KEY ([orderId]) REFERENCES [OrderDone]([orderId])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [ArticlesInOrder]
	ADD CONSTRAINT [R_23] FOREIGN KEY ([articleId]) REFERENCES [Article]([articleId])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Buyer]
	ADD CONSTRAINT [R_14] FOREIGN KEY ([cityId]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Line]
	ADD CONSTRAINT [R_11] FOREIGN KEY ([cityId1]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [Line]
	ADD CONSTRAINT [R_13] FOREIGN KEY ([cityId2]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [OrderDone]
	ADD CONSTRAINT [R_8] FOREIGN KEY ([buyerId]) REFERENCES [Buyer]([buyerId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Sells]
	ADD CONSTRAINT [R_5] FOREIGN KEY ([shopId]) REFERENCES [Shop]([shopId])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [Sells]
	ADD CONSTRAINT [R_6] FOREIGN KEY ([articleId]) REFERENCES [Article]([articleId])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Shop]
	ADD CONSTRAINT [R_4] FOREIGN KEY ([cityId]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [TransactionDone]
	ADD CONSTRAINT [R_10] FOREIGN KEY ([orderId]) REFERENCES [OrderDone]([orderId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [TransactionDoneBuyer]
	ADD CONSTRAINT [R_25] FOREIGN KEY ([transactionId]) REFERENCES [TransactionDone]([transactionId])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [TransactionDoneBuyer]
	ADD CONSTRAINT [R_27] FOREIGN KEY ([buyerId]) REFERENCES [Buyer]([buyerId])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [TransactionDoneShop]
	ADD CONSTRAINT [R_24] FOREIGN KEY ([transactionId]) REFERENCES [TransactionDone]([transactionId])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [TransactionDoneShop]
	ADD CONSTRAINT [R_28] FOREIGN KEY ([shopId]) REFERENCES [Shop]([shopId])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

USE [cm200003]
GO
/****** Object:  StoredProcedure [dbo].[SP_FINAL_PRICE]    Script Date: 29.6.2023. 13:46:16 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SP_FINAL_PRICE]
	@OrderId int,
	@FullPrice decimal(10,3),
	@Discounts decimal(10,3),
	@FinalPrice decimal(10,3) output
AS
BEGIN

	declare @TransactionId int

	select @TransactionId = TransactionDone.transactionId
	from TransactionDone join TransactionDoneBuyer on TransactionDone.transactionId = TransactionDoneBuyer.transactionId
	where TransactionDone.orderId = @OrderId
	
	update TransactionDone
	set amount = @FullPrice - @Discounts
	where transactionId = @TransactionId

	set @FinalPrice = @FullPrice - @Discounts

END

/****** Object:  Trigger [dbo].[TR_TRANSFER_MONEY_TO_SHOPS]    Script Date: 29.6.2023. 13:49:16 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TRIGGER [dbo].[TR_TRANSFER_MONEY_TO_SHOPS]
   ON  [dbo].[OrderDone]
   AFTER update
AS 
BEGIN
	
	if (UPDATE(receivedTime))
	begin 

	declare @cursor cursor
	declare @orderId int
	declare @cursorShops cursor
	declare @shopId int
	declare @amount decimal(10,3)
	declare @dateDone datetime
	declare @transactionId int
	
	set @cursor = cursor for 
	select orderId
	from inserted

	open @cursor

	fetch next from @cursor
	into @orderId

	while @@FETCH_STATUS = 0
	begin

	set @cursorShops = cursor for
	select Shop.shopId, (SUM(ArticlesInOrder.COUNT * Article.articlePrice) - (SUM(ArticlesInOrder.COUNT * Article.articlePrice)*Shop.discount / 100)) - (SUM(ArticlesInOrder.COUNT * Article.articlePrice) - (SUM(ArticlesInOrder.COUNT * Article.articlePrice)*Shop.discount / 100)) * (
		select case 
		when coalesce(SUM(amount),0) > 10000 then 3
		else 5
		end
		from TransactionDone join TransactionDoneBuyer on TransactionDone.transactionId = TransactionDoneBuyer.transactionId
		where DATEDIFF(DAY, dateDone, receivedTime) between 0 and 30
		and TransactionDoneBuyer.buyerId = OrderDone.buyerId
	) / 100, OrderDone.receivedTime
	from OrderDone join ArticlesInOrder on OrderDone.orderId = ArticlesInOrder.orderId
	join Article on ArticlesInOrder.articleId = Article.articleId
	join Sells on Article.articleId = Sells.articleId
	join Shop on Shop.shopId = Sells.shopId
	where OrderDone.receivedTime is not null and OrderDone.state='arrived'
	group by Shop.discount, Shop.shopId, OrderDone.receivedTime, OrderDone.buyerId

	open @cursorShops

	fetch next from @cursorShops
	into @shopId, @amount, @dateDone

	while @@FETCH_STATUS = 0
	begin
		
		insert into TransactionDone (orderId, dateDone, amount) values (@orderId, @dateDone, @amount)
		
		select top 1 @transactionId = transactionId
		from TransactionDone
		order by transactionId desc

		insert into TransactionDoneShop (transactionId, shopId) values (@transactionId, @shopId)

		fetch next from @cursorShops
		into @shopId, @amount, @dateDone
	end

	close @cursorShops
	deallocate @cursorShops

	fetch next from @cursor
	into @orderId
	end

	close @cursor
	deallocate @cursor

	end

END
