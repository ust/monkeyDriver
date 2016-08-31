* scan all the messages that might be bank messages
* user selects word expressions and assigns them to specific domain fields
* persist messages as processed. persist templates
* analyze bank messages suggest possible templates
* command line app as prototype
* save bank numbers and track them in future
* pick messages and transform to banking events
* fire new unrecognized messages to force user accept template
* template could be predefined by community experience
------------------------------------------------------------------------------------------------------------------------
Message
Template
Transaction
Account

          -- create new template --
        /                           \
message                               create new Transaction
        \                           /
          ----- match template ----



