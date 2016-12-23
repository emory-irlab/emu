#!/usr/bin/python
# -*- coding: utf8 -*-
import os
import re
import urllib2
import urlparse
from xml.sax.saxutils import unescape
from sys import argv
import socket
class resource_extractor:
    def __init__(self, new_dir, resource_subdir_suffix = '_files'):
        self.__new_dir = new_dir
        if not os.path.exists(self.__new_dir):
            os.mkdir(self.__new_dir)
        self.resource_subdir_suffix = resource_subdir_suffix
        self.__new_resource_dir = ''
        self.__current_html_path = ''
        self.__current_url = ''
        self.__stored_cache = {}
        self.regexp_template = """(?P<prefix><{0}.*?{1}\s*=\s*['"])(?P<url>[^'"]*{2})(?P<suffix>['"][^\>]*>)"""

    def __get_random_filename(self):
        from random import choice, randint
        import string
        length = randint(5,50)
        return ''.join(choice(string.ascii_lowercase + string.ascii_uppercase + string.digits) for x in xrange(length))

    def __store_img_from_css(self, css_code, css_url = None):
        img_incss_regexp = re.compile("""(?P<prefix>url\(['"]??)(?P<url>[^\)"']*(?:png|jpg|jpeg|gif|jpeg|bmp|pcx))(?P<suffix>["']??\))""", flags = re.I)
        temp = self.__current_url
        if css_url:
            self.__current_url = css_url
        css_code = re.sub(img_incss_regexp, self.get_resource_name, css_code)
        self.__current_url = temp
        return css_code

    # Downloads resource and returns its new path
    def store_resource(self, resource_path):
        # Strip last slash is any
        resource_path = resource_path.rstrip("/")        
        # Replace '//www.url.com' with http://www.url.com
        if resource_path.startswith("//"):
            resource_path = "http:" + resource_path
        resource_path = unescape(resource_path)

        if resource_path in self.__stored_cache:
            return self.__stored_cache[resource_path]
        resource_url = urlparse.urljoin(self.__current_url, resource_path)
        try:
            # Fix for the problem with Forbidden when try to download wikipedia resources
            opener = urllib2.build_opener()
            opener.addheaders = [('User-agent', 'Mozilla/5.0')]
            data = opener.open(resource_url)
        except urllib2.URLError, e:
            # supress error printing
            #print "Error: ", e
            #self.__stored_cache[resource_path] = resource_path
            return resource_path
        
        # If original filename is too complex, let's generate random
        if resource_path.find("&") != -1 or len(resource_path) > 128:
            basename = self.__get_random_filename()
        else:
            basename = os.path.basename(resource_path)
        new_resource_filename = os.path.basename(self.__current_html_path) \
                + self.resource_subdir_suffix + '/' + basename
        new_resource_path = os.path.join(self.__new_dir, new_resource_filename)
        (_, ext) = os.path.splitext(new_resource_filename)
        resourceData = data.read()
        if ext == '.css':
            resourceData = self.__store_img_from_css(resourceData, resource_url)

        out = open(new_resource_path, 'w')
        print >> out, resourceData
        out.close()
        self.__stored_cache[resource_path] = new_resource_filename
        return new_resource_filename

    # downloads resource and returns its new name
    def get_resource_name(self, match_obj):
        return match_obj.group(1) + self.store_resource(match_obj.group(2)) + match_obj.group(3)

    def extract_html_resources(self, page_url, html_file):
        self.__stored_cache = {}
        self.__current_url = page_url
        self.__current_html_path = html_file
        
        self.__new_resource_dir = os.path.join(self.__new_dir, os.path.basename(html_file) + self.resource_subdir_suffix)
        if not os.path.exists(self.__new_resource_dir):
            os.mkdir(self.__new_resource_dir)
        
        html_code = ''
        inp = open(html_file, 'r')
        html_code = inp.read()
        inp.close()
        
        # Store css
        css_regexp = re.compile(self.regexp_template.format('link','href','(?:/?[^"\']*)?'), flags = re.I)
        html_code = re.sub(css_regexp, self.get_resource_name, html_code)
        # Store img
        img_regexp = re.compile(self.regexp_template.format('img','src','(?:png|jpg|jpeg|gif|jpeg|bmp|pcx)'))
        html_code = re.sub(img_regexp, self.get_resource_name, html_code)
        # store img from css
        html_code = self.__store_img_from_css(html_code)        
        # Store js
        js_regexp = re.compile(self.regexp_template.format('script','src','(?:js|vb)'))
        html_code = re.sub(js_regexp, self.get_resource_name, html_code)
        
        new_path = os.path.join(self.__new_dir + os.path.basename(html_file))
        out = open(new_path, 'w')
        print >> out, html_code
        out.close()

if __name__ == '__main__':
    if len(argv) != 4:
        print "Wrong number of arguments, should be html_resource_save.py <input.html> <input html url> <dir to save new html file>"
        exit()
    socket.setdefaulttimeout(10)
    extractor = resource_extractor(argv[3])    
    #extractor = resource_extractor('/Users/denxx/Documents/workspace/full_html_save/src/new_test/')
    extractor.extract_html_resources(argv[2], argv[1])
    #extractor.extract_html_resources('http://www.google.com/search?q=Python+regexp+match+any+string+except&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a#sclient=psy&hl=en&client=firefox-a&rls=org.mozilla:en-US%3Aofficial&source=hp&q=Emory+irlab&pbx=1&oq=Emory+irlab&aq=f&aqi=g-v1&aql=&gs_sm=e&gs_upl=1485223l1487596l0l1487841l13l10l1l2l2l0l280l853l4.2.1l7l0&bav=on.2,or.r_gc.r_pw.r_cp.&fp=12392ff7c6c9f28&biw=1401&bih=769', '/Users/denxx/Documents/workspace/full_html_save/src/test/search2.html')
    