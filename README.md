# Web Crawler

Build a web crawler that starts from the seed URL and returns the top 10 most expensive books found on the site.

## Seed URL
https://books.toscrape.com/

## Requirements:
1. Start crawling from the seed URL.
2. Discover and follow links to category pages and product pages.
3. Avoid visiting the same URL more than once.
4. Extract: Book title, Price, Availability and Product URL
5. Return the 10 most expensive books.
6. Do not follow links outside the domain of the seed URL.

### Follow-ups:

1. Crawl a maximum of 5 pages concurrently. Ensure no two threads crawl the same page.