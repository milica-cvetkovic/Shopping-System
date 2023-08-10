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
	where OrderDone.receivedTime is not null
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
