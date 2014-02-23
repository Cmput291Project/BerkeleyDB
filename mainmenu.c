#include "header.h"

char scopy[128];
FILE * report;
struct timeval start, stop;

int main(int argc,char **argv) {

  int ret;
  DB *db, *sdb;  
  DBC *dbcp;
  
  db_create(&db, NULL, 0);
  db_create(&sdb, NULL, 0);
  //db->set_flags(db, DB_DUP);
  sdb->set_flags(sdb, DB_DUP);  

  if(argc != 2){
    fprintf(stderr, "Error, incorrect command line arguments provided.\n Please enter type 'mydbtest db_type_option'\n");
    abort();}
  printf("Welcome to the Main Menu!\n");
  printf("Please enter the corresponding number of the option you would like to access:\n");
  printf("\n");
  printf("1. Create and populate the database\n");
  printf("2. Retrieve records with a given key\n");
  printf("3. Retrieve records with a given data\n");
  printf("4. Retrieve records within a given range of key values\n");
  printf("5. Destroy the database\n");
  printf("6. Quit\n");
  char input[10];
  int option;

  report=fopen("answers.txt","a");
  fgets(input,sizeof(input),stdin);
  sscanf(input,"%d",&option);
  switch(option) {
  case 1:
    //call populate function
	//destroy_db(db, atoi(argv[1]), sdb); 
	open_db(db,atoi(argv[1]), sdb);
	populate_db(db, atoi(argv[1]), sdb);
    break; 
  case 2:
    //call retrieve records by key function
	open_db(db, atoi(argv[1]), sdb);
	getby_key(db, atoi(argv[1]), sdb);
    break;
  case 3:
    //call retrieve record by data function
	open_db(db, atoi(argv[1]), sdb);
	getby_data(db, atoi(argv[1]), sdb);
    break;
  case 4:
    //call retrieve by range function
	open_db(db, atoi(argv[1]), sdb);
	getby_range(db, atoi(argv[1]), sdb);
    break;
  case 5: 
    //call destroy function
	//open_db(db, atoi(argv[1]), sdb);
	destroy_db(db,atoi(argv[1]), sdb);
	return 0;
    break;
  case 6:
    //call quit & drop function
    break;
  default:
    printf("Error! Invalid input!/n");
    abort();
  }

  db->close(db,0);
  sdb->close(sdb,0);
  return 0;
}

int create_db(DB *db, int type, DB *sdb) {
	if(type == 1) 
		db_create(&db,NULL, 0);
	else if(type == 2) 
		db_create(&db,NULL, 0);
	else if(type == 3) {
		db_create(&db, NULL, 0);
		db_create(&sdb, NULL, 0);
		open_db(db, 3, sdb);
		//db->associate(db, NULL, sdb, get_secondary, 0);
	}
}

int open_db(DB *db, int type, DB *sdb) {
	if(type == 1) 
		db->open(db, NULL, DB_LOC_HASH, NULL, DB_HASH, DB_CREATE, 0664);  
	else if(type == 2) 
		db->open(db, NULL, DB_LOC_BTREE, NULL, DB_BTREE, DB_CREATE, 0664);
	else if(type == 3) {
		db->open(db, NULL, DB_LOC_INDEX, NULL, DB_BTREE, DB_CREATE, 0644);
		sdb->open(sdb, NULL, DB_LOC_SINDEX, NULL, DB_BTREE, DB_CREATE, 0644);
		//db->associate(db, NULL, sdb, get_secondary, 0);
	}	
}

int populate_db(DB *db, int type, DB *sdb) {

	int ret,range,index,i;
  DBT key, data, search_key, search_data;
  DBC *dbc;
  unsigned seed;
 
  char keybuff[128];
  char databuff[128];

  /*
   *  to create a db handle
   */
  /*if ( (ret = db_create(&db, NULL, 0)) != 0 ) {
    printf("db_create: %s\n", db_strerror(ret));
    exit(1);
  }*/

  memset(&key, 0, sizeof(key));
  memset(&data, 0, sizeof(data));
 
  /*
   *  to seed the random number after db openning, and see it once.
   */
  seed = 10000000;
  srand(seed);

  /*
   *  to populate the database
   */
  for (index=0;index<DB_SIZE;index++)  {

    // to generate the key string
    range=64+random()%(64);
    for (i=0; i<range;i++)
      keybuff[i]= (char)(97+random()%26);
    keybuff[range]=0;
       
    key.data = keybuff; 
    key.size = range; 

    // to generate the data string
    range=64+random()%(64);
    for (i=0;i<range;i++)
      databuff[i]= (char) (97+random()%26);
    databuff[range]=0;

    data.data=databuff;
    data.size=range;

    // You may record the key/data string for testing
   /* if(index/10000 == 1) {
		printf("%s\n",(char *)key.data);
		printf("%s\n\n",(char *)data.data);
	}*/
		
		if (ret=db->put(db, NULL, &key, &data, 0))
			printf("DB->put: %s\n", db_strerror(ret));
		if(type == 3) {
			if(ret = sdb->put(sdb, NULL, &data, &key, 0))
				printf("DB->put: %s\n", db_strerror(ret));
		}
  }

}

int getby_data(DB *db, int type, DB *sdb) {
	int ret = 1;
	DBT data;
    DBT key, skey;
	DBC *dbc;
	char input[256];
	int count=0;
  fgets(input,sizeof(input),stdin);
  size_t size = strlen(input) - 1;
  input[size] = '\0';
 	memset(&data, 0 , sizeof(data));
  memset(&key, 0 , sizeof(key));
	memset(&skey, 0 , sizeof(key));
	
	if(type == 3) {	
		//strncpy(scopy, input, 64);
		//data.data = scopy;
		//db = sdb;
		sdb->cursor(sdb, NULL, &dbc, 0);
		gettimeofday(&start,NULL);
		while(ret != DB_NOTFOUND) {
    //Appending each found record to the file:
    	ret = dbc->c_get(dbc, &key ,&data, DB_NEXT);
			if(strcmp(input,key.data) == 0 ) {
				fprintf(report,"%s\n",(char*)data.data);
				fprintf(report,"%s\n",(char*)key.data);
				fprintf(report,"\n");
				count++;
			}
		}
		gettimeofday(&stop,NULL);
	}

	else {
		//data.data = input;
		//data.size = sizeof(1 + strlen(scopy)*sizeof(char));
		db->cursor(db, NULL, &dbc, 0);		
	
	//ret = dbc->c_get(dbc, &key, &data, DB_NEXT);
  //code used to find duplicates of data object in db
  		gettimeofday(&start,NULL);
  		while(ret != DB_NOTFOUND) {
    //Appending each found record to the file:
    		ret = dbc->c_get(dbc, &key, &data, DB_NEXT);
			if(strcmp(input,data.data) == 0 ) {
				fprintf(report,"%s\n",(char*)key.data);
				fprintf(report,"%s\n",(char*)data.data);
				fprintf(report,"\n");
				count++;
			}
  		}
		gettimeofday(&stop,NULL);
	}
	
	printf("Retrieve by data took %lu microseconds and recieved %d records.\n",stop.tv_usec-start.tv_usec,count);
	/*Note that this will only work for subsecond times as tv.usec will loop. For the general case use a combination of tv_sec & tv_usec*/
	

}

getby_key(DB *db, int type, DB *sdb) {

	int ret=1;
	DBT data;
  DBT key;
	DBC *dbc;
	char input[256];
	int count=0;
  	fgets(input,sizeof(input),stdin);
	size_t size = strlen(input) - 1;
  input[size] = '\0';
 	memset(&key, 0 , sizeof(key));
  memset(&data, 0 , sizeof(data));
	
	/*if(type == 3) {	
		strncpy(scopy, input, 64);
		key.data = scopy;
		key.size = sizeof(1 + strlen(scopy)*sizeof(char));
		sdb->cursor(sdb, NULL, &dbc, 0);		
	}
	else {*/
		//key.data = input;
		//key.size = sizeof(1 + strlen(input)*sizeof(char));
		db->cursor(db, NULL, &dbc, 0);		
	//}
	
	//ret = dbc->c_get(dbc, &key, &data, DB_SET);
  //code used to find duplicates of data object in db
  gettimeofday(&start,NULL);
  while(ret != DB_NOTFOUND) {
    //Appending each found record to the file:
    ret = dbc->c_get(dbc, &key, &data, DB_NEXT);
	if((strcmp(input, key.data )) == 0) {
		fprintf(report,"%s\n",(char*)key.data);
		fprintf(report,"%s\n",(char*)data.data);
		fprintf(report,"\n");
		count++;
	}
  }
	gettimeofday(&stop,NULL);
	printf("Retrieve by data took %lu microseconds and recieved %d records.\n",stop.tv_usec-start.tv_usec,count);
	/*Note that this will only work for subsecond times as tv.usec will loop. For the general case use a combination of tv_sec & tv_usec*/
}

int getby_range(DB *db, int type, DB *sdb) {
	int ret=1;
	DBT key;
  DBT data;
	DBC *dbc;
  int count=0;
	char input1[256];
	char input2[256];
 	fgets(input1,sizeof(input1),stdin);
	size_t size = strlen(input1) - 1;
  input1[size] = '\0';
	fgets(input2, sizeof(input2), stdin);
	size = strlen(input2) - 1;
  input2[size] = '\0';
 	memset(&key, 0 , sizeof(key));
  memset(&data, 0 , sizeof(data));
	
	//key1.data = input1;
	//key.size = (1 + strlen(input1) * sizeof(char));

	//key2.data = input2;
	//key2.size = (1 + strlen(input2) * sizeof(char));
	db->cursor(db, NULL, &dbc, 0);

  int comp = strcmp(input1, input2);
  if(comp > 0) {
    printf("Error : String1 is not less than String 2\n");
    return 0;
  }
	//ret = dbc->c_get(dbc, &key1, &data, DB_SET);
  //code used to find duplicates of data object in db
  gettimeofday(&start,NULL);
  while(ret != DB_NOTFOUND) {
     //Appending each found record to the file:
    ret = dbc->c_get(dbc, &key, &data, DB_NEXT);
	
	if(strcmp(input1, key.data) <= 0 &&
		strcmp(key.data, input2) <= 0) {
		
		fprintf(report,"%s\n",(char*)key.data);
		fprintf(report,"%s\n",(char*)data.data);
		fprintf(report,"\n");
		count++;
	}
  }
	gettimeofday(&stop,NULL);
    printf("Retrieve by range took %lu microseconds and returned %d records.\n",stop.tv_usec-start.tv_usec,count);
	/*Note that this will only work for subsecond times as tv.usec will loop. For the general case use a combination of tv_sec & tv_usec*/

}

int destroy_db(DB *db, int type, DB *sdb) {
	if(type == 1) 
		db->remove(db, DB_LOC_HASH, NULL, 0);
	else if(type == 2) 
		db->remove(db, DB_LOC_BTREE, NULL, 0);
	else if(type == 3) {
		db->remove(db, DB_LOC_INDEX, NULL, 0); 
		sdb->remove(sdb, DB_LOC_SINDEX, NULL, 0);
	}
}

int get_secondary(DB *secondary, const DBT *pkey, const DBT *pdata, DBT *skey) {
	memset(skey, 0, sizeof(DBT));

	strncpy(scopy, pdata->data, 64);
	skey->data = scopy;
	skey->size = sizeof(1 + strlen(scopy)*sizeof(char));
	return 0;
}

