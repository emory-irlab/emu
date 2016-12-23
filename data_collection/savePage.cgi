#!/usr/bin/perl 
use CGI; # load CGI routines
use strict;
use File::Path; # has a function to create all directories in the path


my $q = new CGI; 


my $id=$q->param('content_id');
my $data=$q->param('data');
my $wid=$q->param('wid');


my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $yy = int($year) + 1900;


my $dir_path =  "./serps/$yy/$mon/$mday/$wid"; 


if (-d $dir_path) {
#no need to create new dir
}
else {
mkpath($dir_path) or die "Cannot create dir : $!" ; # create new dir
}


open F, "> $dir_path/$id" or die "Can't open $id : $!";
print F $data;
close F;


my $link="<a href=" . '"' . "$dir_path/$id" . '">' . "$id</>";


print $q->header, # create the HTTP header
$q->start_html('content saved'), # start the HTML
$q->h1("saved file $link"),  
$q->end_html; 