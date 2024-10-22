
#include <stdlib.h>
#include <db.h>
#include <string.h>
#include <stdio.h>
#include <sys/time.h>

#define DB_SIZE   100000
#define DB_LOC_HASH "/tmp/tsajed_db/hash.db"
#define DB_LOC_BTREE "/tmp/tsajed_db/btree.db"
#define DB_LOC_INDEX "/tmp/tsajed_db/index.db"
#define DB_LOC_SINDEX "/tmp/tsajed_db/sindex.db"

int create_db(DB *, int, DB *);
int open_db(DB *, int, DB *);
int populate_db(DB *, int, DB *);
int getby_data(DB *, int, DB *);
int getby_key(DB *, int, DB *);
int getby_range(DB *, int, DB *);
int destroy_db(DB *, int, DB *);
int get_secondary(DB *, const DBT *, const DBT *, DBT *);
