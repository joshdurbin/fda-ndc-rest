# fda-ndc-rest 
FDA National Drug Registry REST service built atop Ratpack, MxMongo hosted at Heroku.

Data load procedures:

1. Goto: http://www.fda.gov/Drugs/InformationOnDrugs/ucm142438.htm
2. Download the file "NDC Database File (Zip Format)"
3. Unpack "NDC Database File (Zip Format)" and note the two txt and two Excel files. The text files are tab delimited files (TSV).
4. With the latest Java 8 JDK and [Groovy](http://www.groovy-lang.org/) installed (recommend via [sdkman](http://sdkman.io/)), execute the [TSV conversion script](https://raw.githubusercontent.com/joshdurbin/fda-ndc-rest/data_cleaning_scripts/FormatNDCData.groovy) found in the [data-cleaning-scripts](https://github.com/joshdurbin/fda-ndc-rest/tree/data_cleaning_scripts) branch.
4. Place the script in the same directory as the `package.txt` and `product.txt` files and execute. It will produce a JSON file, `fdaProductsForImport.json`.
5. Import script output into Mongo

  * `mongoimport -v --host=127.0.0.1 --port=27017 --db fda-ndc-rest --collection products fdaProductsForImport.json`

7. Establish indexes on collection

  ```javascript
  db.products.createIndex({productNDC: 1}, {name: "NDC Code Index"})
  db.products.createIndex({applicationNumber: 1}, {name: "Application Number Index"})
  db.products.createIndex({labelerName: 1}, {name: "Labeler Name Index"})
  db.products.createIndex({startMarketingDate: 1,
    endMarketingDate: 1}, {name: "Marketing Start/End Date Index"})
  db.products.createIndex({productTypeName: 1}, {name: "Product Type Index"})
  db.products.createIndex({marketingCategoryName: 1}, {name: "Marketing Category Name Index"})
```

8. Establish text indexes on collection

  ```javascript
  db.products.createIndex(
  {
    "$**": "text"
  },
  {
    weights: {
      proprietaryName: 5,
      labelerName: 4,
      nonProprietaryName: 3,
      "substances.name": 2,
      "pharmacologicalClassCategories.name": 2,
    },
    name: "Text Index w/ Weights"
  }
)
```

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy?template=https://github.com/joshdurbin/fda-ndc-rest)  
