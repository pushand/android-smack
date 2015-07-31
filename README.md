# AndroidSmackLib

This library provides layer for easy xmpp implementation which uses smack library

Usage
-----

    //step 1 basically the class that would like to receive buddy list
    implement XmppListener

	//step 2 your class that represents a Buddy should implement ChatApi in this case its Friend
    eg : Friend implements ChatApi


	//step 3 make connection
    new XmppLib.XmppLibBuilder().username("jabberId").password("password")
                    .host("xmpp-server-domain").
                    port(5222).serviceName("service-name").build(this);


    //step 4 handle buddie list received from XmppListener
    @Override
        public void buddies(List<String> buddies) {
            for (String buddy : buddies){
                Friend friend = new Friend(this);
                friend.setName(buddy);
                friendList.add(friend);
            }
            //step 5 register your list with library
            XmppLib.register(friendList);
        }
