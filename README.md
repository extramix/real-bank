# What's this?

I find that I learn a lot more with autocomplete and minimal AI usage, using it only as a chatbot, so this is a project
that
will obviously evolve slowly, but somehow it does help me internalize these concepts deeply, so I think that's a good
tradeoff.

That's not to say that using AI is bad. I do have side projects where I use the agentic engineering approach too, but I
find that it's a totally different skill set. Successfully building a product and learning how the system
works might leave some of the hands-on technical work behind, and that's totally okay.

Both sides might argue, but I think a little bit of balance can't hurt. You can use AI when you need to and turn off AI
when you have to.

# Stage 1 - Pure Java

After a bit of research, I found that you're not supposed to use float and double with anything that needs precise
decimals. The reason comes from how those primitives store numbers. They use base 2, which
means there are some values (like 0.1) that can't be represented exactly.

Now the choice between BigDecimal and long comes down to how I want to manage the amounts. Yes, I could pick 1L to
represent 1 cent for USD. Long can work with multiple currencies too, as long as I keep track of each currency and its
units. But for this app, I find BigDecimal easier to manage when converting currencies and deciding how to round.

I also round the amount ONLY when currency conversion happens, using HALF_UP to 2 decimal places. Amounts credited to
or debited from an account must fit within 2 decimal places. A foreign deposit can start with more decimal places,
since we convert and round it before crediting the account.

The transfer lock is there to protect against multithreading issues. At this time, it makes transfers on the same Bank
instance take turns. Deposits, withdrawals, and direct account access can still happen during a transfer, and I aim to
improve that later!
