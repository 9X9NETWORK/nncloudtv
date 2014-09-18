import urllib2
import os

host = "localhost:8080"
url = 'http://' + host + '/billingAPI/recurringCharge'

print urllib2.urlopen(url).read()

