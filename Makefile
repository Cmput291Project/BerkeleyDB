#Makefile for C291 project 2 by Sabrina Gannon and Tanvir Sajed

mydbtest: mainmenu.c
	gcc -g -o mydbtest mainmenu.c -ldb

tar:
	tar -cvf proj2.tar mainmenu.c header.h Makefile