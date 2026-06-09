## Custom Data

This project has been designed to accept customized data files for items, prices, and availability, which can be used to 
create scenarios specific to your business needs. For example, let's say you wanted to focus on your case study on just 
a single item.

Create a CSV file with the following format:

`custom-items.csv`
```csv
item_id,small_description,long_description,category,merch_class,channel_restriction,barcode,brand_name,age_restriction,primary_image,alternate_image,base_url
12954218,Mac n Cheese,Macaroni & Cheese,PREPARED_DINNER,2,MULTI-CHANNEL,123456284025,Kraft,0,primary_mac.jpeg,secondary_mac.jpeg,https://target.com
```

And then add a file for prices:

`custom-prices.csv`
```csv
item_id,regular_price,sale_price,type
12954218,1.99,1.59,SALE
```

We now need to craft a docker-compose file that mounts these two files into the `/data` directory of the container and then
provides the correct environment variables to the application. You can use the variables `ITEM_DATA_FILE`, `PRICE_DATA_FILE`, and `AVAILABILITY_DATA_FILE` 
to specify the location of the files inside the container. These environment values will override the defaults of `item.data-file`, `price.data-file`, and `availability.data-file`, respectively
in the application configuration.

Here's an example using a custom item and price file:

`custom-docker-compose.yml`
```yaml
services:
  app:
    build:
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      ITEM_DATA_FILE: /data/custom-items.csv
      PRICE_DATA_FILE: /data/custom-prices.csv
    volumes:
      - ./custom-items.csv:/data/custom-items.csv
      - ./custom-prices.csv:/data/custom-prices.csv
```

Finally, you can launch the application with the custom data by running:

```sh
docker-compose -f custom-docker-compose.yml up
```

Or alternatively, you can use the `docker run` command directly:
```sh
docker build . -t my-retail-services
docker run --rm -p 8080:8080 \
  -e ITEM_DATA_FILE=/data/custom-items.csv \
  -e PRICE_DATA_FILE=/data/custom-prices.csv \
  -v $(pwd)/custom-items.csv:/data/custom-items.csv \
  -v $(pwd)/custom-prices.csv:/data/custom-prices.csv \
  my-retail-services
```


## Data Formats

All files are expected to be in CSV format, with the first row of each file is a header row that describes the columns.

### Items
See the default file at src/main/resources/items.csv

| column name          | description                                                                            | sample value            |
|----------------------|----------------------------------------------------------------------------------------|-------------------------|
| item_id              | numeric, the unique item identifier                                                    | 123456                  |
| small_description    | string, brief description of the item                                                  | GG Milk                 |
| long_description     | string, detailed description of the item                                               | Good&Gather Milk 2%     |
| category             | string, the department the item belongs to                                             | DAIRY                   |
| merch_class          | numeric, the merchandise classification code                                           | 2                       |
| channel_restriction  | string, sales channel limitation.  SHould be one of 'MULTI-CHANNEL', 'IN-STORE', 'ONLINE' | MULTI-CHANNEL           |
| barcode              | numeric, unique barcode identifier                                                     | 123456284025            |
| brand_name           | string, brand name of the item                                                         | Good&Gather             |
| age_restriction      | numeric, minimum age required to purchase. Use `0` for no restriction                  | 0                       |
| primary_image        | string, URI of main product image                                                      | primary_image_milk.jpeg |
| alternate_image      | string, URI of secondary product image                                                 | alternate_image_milk.jpeg |
| base_url             | string, base URL for product information                                               | https://target.com      |

**Example File**
```csv
item_id,small_description,long_description,category,merch_class,channel_restriction,barcode,brand_name,age_restriction,primary_image,alternate_image,base_url
123456,GG Milk,Good&Gather Milk 2%,DAIRY,2,MULTI-CHANNEL,123456284025,Good&Gather,0,primary_image_milk.jpeg,alternate_image_milk.jpeg,https://target.com
789123,GG Cheese,Good&Gather Colby Jack Cheese,DAIRY,2,MULTI-CHANNEL,789123284025,Good&Gather,0,primary_image_cheese.jpeg,alternate_image_cheese.jpeg,https://target.com
456788,TSHIRT MEN,All In Motion TShirt Men's,APPAREL,25,ONLINE,456788284025,All In Motion,0,primary_image_tshirt.jpeg,alternate_image_tshirt.jpeg,https://target.com
987612,COUGH & COLD SYP,Up&Up Cough & Cold Syrup,HEALTH,51,IN-STORE,987612284025,Up&Up,21,primary_image_cough.jpeg,alternate_image_cough.jpeg,https://target.com
```
### Prices
See the default file at src/main/resources/prices.csv

| column name   | description                                                              | sample value |
|---------------|--------------------------------------------------------------------------|--------------|
| item_id       | numeric, the item identifier - should match the item_id in the item data | 123456       |
| regular_price | numeric, the price of the item                                           | 10.25        |
| sale_price    | numeric, the sale price of the item                                      | 8.99         |
| type          | string, the type of price.  Should be one of `REGULAR`, `SALE`           | REGULAR      |

**Example File**

```csv
item_id,regular_price,sale_price,type
123456,10,8,REGULAR
789123,9.45,8.99,REGULAR
456788,32.4,10.11,REGULAR
987612,15.0,12.0,SALE
```

### Availability
See the default file at src/main/resources/availability.csv

| column name                | description                                                              | sample value |
|----------------------------|--------------------------------------------------------------------------|--------------|
| item_id                    | numeric, the item identifier - should match the item_id in the item data | 123456       |
| available_units            | numeric, the current quantity available in inventory                     | 10           |
| limited_quantity_threshold | numeric, threshold for marking the item as having limited stock          | 2            |

**Example File**
```csv
item_id,available_units,limited_quantity_threshold
123456,10,2
789123,90,10
456788,300,20
987612,600,20
```
