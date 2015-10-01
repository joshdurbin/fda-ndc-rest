# fda-ndc-rest 

Data load procedures:

1. Goto: http://www.fda.gov/Drugs/InformationOnDrugs/ucm142438.htm
2. Download the file "NDC Database File (Zip Format)"
3. Unpack "NDC Database File (Zip Format)" and note the two txt and two Excel files. The text files are tab delimited files.
4. Execute imports into mongo

  * `mongoimport -v --host=127.0.0.1 --port=27017 --db fda_national_drug_codes --collection products --type=tsv --headerline product.txt`
  * `mongoimport -v --host=127.0.0.1 --port=27017 --db fda_national_drug_codes --collection packaging --type=tsv --headerline package.txt`

5. Execute updates to property names for "`products`" collection

  ```javascript
    db.products.update(
    {},{
    $rename: { 'PRODUCTID': 'productID',
      'PRODUCTNDC': 'productNDC',
      'PRODUCTTYPENAME': 'productTypeName',
      'PROPRIETARYNAME': 'proprietaryName',
      'PROPRIETARYNAMESUFFIX': 'proprietaryNameSuffix',
      'NONPROPRIETARYNAME': 'nonProprietaryName',
      'DOSAGEFORMNAME': 'dosageFormName',
      'ROUTENAME': 'routeName',
      'STARTMARKETINGDATE': 'startMarketingDate',
      'ENDMARKETINGDATE': 'endMarketingDate',
      'MARKETINGCATEGORYNAME': 'marketingCategoryName',
      'APPLICATIONNUMBER': 'applicationNumber',
      'LABELERNAME': 'labelerName',
      'SUBSTANCENAME': 'substanceName',      
      'ACTIVE_NUMERATOR_STRENGTH': 'activeNumeratorStrength',
      'ACTIVE_INGRED_UNIT': 'activeIngredUnit',
      'PHARM_CLASSES': 'pharmClasses',
      'DEASCHEDULE': 'deaSchedule' }
    },
    false,
    true )
```

6. Execute updates to property names for "`packaging`" collection

  ```javascript
    db.packing.update( 
    {},{ 
    $rename: { 'PRODUCTID': 'productID', 
      'PRODUCTNDC': 'productNDC', 
      'NDCPACKAGECODE': 'ndcPackageCode', 
      'PACKAGEDESCRIPTION': 'packageDescription'} 
    }, 
    false, 
    true )
```    

7. Establish indexes on "`drugs`" collection

  * `db.products.createIndex( { productID: 1, productNDC: 1, labelerName: 1, productTypeName: 1 } )`

8: Establish indexes on "`packaging`" collection

  * `db.packagin.createIndex( { productID: 1, productNDC: 1, ndcPackageCode: 1 } )`
