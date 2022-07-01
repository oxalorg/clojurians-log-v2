(ns src.clojurians-log.slack.events-test
  "Testing live events received via socket mode"
  (:require [clojure.edn :as edn]))

(def message-event
  {:attachments nil,
   :blocks '({:block-id "3SH0A",
              :elements ({:elements ({:style nil, :text "2", :type "text"}),
                          :type "rich_text_section"}),
              :type "rich_text"}),
   :bot-id nil,
   :bot-profile nil,
   :channel "C020GQ8LW85",
   :channel-type "channel",
   :client-msg-id "c9f5e93d-9255-4ce1-af21-503253af4783",
   :edited nil,
   :event-ts "1646720669.941239",
   :files nil,
   :parent-user-id nil,
   :team "T01G3T4BVGE",
   :text "2",
   :thread-ts nil,
   :ts "1646720669.941239",
   :type "message",
   :user "U01FVSUGVN3"})

(def message-replied-event
  {:attachments nil,
   :blocks '({:block-id "R61n",
              :elements ({:elements
                          ({:style nil, :text "replied", :type "text"}),
                          :type "rich_text_section"}),
              :type "rich_text"}),
   :bot-id nil,
   :bot-profile nil,
   :channel "C020GQ8LW85",
   :channel-type "channel",
   :client-msg-id "ef852c39-c775-4755-b11b-b5dc1db33a3d",
   :edited nil,
   :event-ts "1646720744.498229",
   :files nil,
   :parent-user-id "U01FVSUGVN3",
   :team "T01G3T4BVGE",
   :text "replied",
   :thread-ts "1646720669.941239",
   :ts "1646720744.498229",
   :type "message",
   :user "U01FVSUGVN3"})

(def message-changed-event
  {:previous-message
   {:client-msg-id "c9f5e93d-9255-4ce1-af21-503253af4783",
    :starred false,
    :attachments nil,
    :type "message",
    :pinned-to nil,
    :edited nil,
    :ts "1646720669.941239",
    :team "T01G3T4BVGE",
    :files nil,
    :display-as-bot nil,
    :blocks '({:block-id "3SH0A",
               :elements ({:elements ({:style nil, :text "2", :type "text"}),
                           :type "rich_text_section"}),
               :type "rich_text"}),
    :source-team nil,
    :user-team nil,
    :user "U01FVSUGVN3",
    :x-files nil,
    :reactions nil,
    :upload nil,
    :subtype nil,
    :text "2"},
   :channel "C020GQ8LW85",
   :type "message",
   :ts "1646720812.001200",
   :event-ts "1646720812.001200",
   :hidden true,
   :channel-type "channel",
   :message {:client-msg-id "c9f5e93d-9255-4ce1-af21-503253af4783",
             :starred false,
             :attachments nil,
             :type "message",
             :pinned-to nil,
             :edited {:ts "1646720812.000000", :user "U01FVSUGVN3"},
             :ts "1646720669.941239",
             :team "T01G3T4BVGE",
             :files nil,
             :display-as-bot nil,
             :blocks '({:block-id "KjO",
                        :elements ({:elements ({:style nil,
                                                :text "2 become 3",
                                                :type "text"}),
                                    :type "rich_text_section"}),
                        :type "rich_text"}),
             :source-team "T01G3T4BVGE",
             :user-team "T01G3T4BVGE",
             :user "U01FVSUGVN3",
             :x-files nil,
             :reactions nil,
             :upload nil,
             :subtype nil,
             :text "2 become 3"},
   :subtype "message_changed"})

(def message-deleted-tombstone-event
  {:channel "C020GQ8LW85",
   :channel-type "channel",
   :event-ts "1646720906.001300",
   :hidden true,
   :message {:attachments nil,
             :blocks nil,
             :client-msg-id nil,
             :display-as-bot nil,
             :edited nil,
             :files nil,
             :pinned-to nil,
             :reactions nil,
             :source-team nil,
             :starred false,
             :subtype "tombstone",
             :team nil,
             :text "This message was deleted.",
             :ts "1646720669.941239",
             :type "message",
             :upload nil,
             :user "USLACKBOT",
             :user-team nil,
             :x-files nil},
   :previous-message {:attachments nil,
                      :blocks '({:block-id "KjO",
                                 :elements ({:elements ({:style nil,
                                                         :text "2 become 3",
                                                         :type "text"}),
                                             :type "rich_text_section"}),
                                 :type "rich_text"}),
                      :client-msg-id "c9f5e93d-9255-4ce1-af21-503253af4783",
                      :display-as-bot nil,
                      :edited {:ts "1646720812.000000", :user "U01FVSUGVN3"},
                      :files nil,
                      :pinned-to nil,
                      :reactions nil,
                      :source-team nil,
                      :starred false,
                      :subtype nil,
                      :team "T01G3T4BVGE",
                      :text "2 become 3",
                      :ts "1646720669.941239",
                      :type "message",
                      :upload nil,
                      :user "U01FVSUGVN3",
                      :user-team nil,
                      :x-files nil},
   :subtype "message_changed",
   :ts "1646720906.001300",
   :type "message"})

(def reaction-added-event
  {:event-ts "1646726470.004000",
   :item {:channel "C035Z8MP3EZ",
          :file nil,
          :file-comment nil,
          :ts "1646723905.573589",
          :type "message"},
   :item-user "U01FVSUGVN3",
   :reaction "+1",
   :type "reaction_added",
   :user "U01FVSUGVN3"})

(def reaction-removed-event
  {:event-ts "1646726509.004100",
   :item {:channel "C035Z8MP3EZ",
          :file nil,
          :file-comment nil,
          :ts "1646723905.573589",
          :type "message"},
   :item-user "U01FVSUGVN3",
   :reaction "+1",
   :type "reaction_removed",
   :user "U01FVSUGVN3"})

(def channel-created-event
  {:channel {:channel true,
             :created 1646727892,
             :creator "U01FVSUGVN3",
             :id "C036SA9AWSC",
             :name "testnew",
             :name-normalized "testnew",
             :org-shared false,
             :shared false},
   :event-ts "1646727892.004000",
   :type "channel_created"})

(def channel-renamed-event
  {:channel {:channel true,
             :created 1646727892,
             :id "C036SA9AWSC",
             :mpim false,
             :name "testnew222",
             :name-normalized "testnew222"},
   :event-ts "1646727936.004300",
   :type "channel_rename"})

(def channel-joined-event
  {:channel "C020GQ8LW85",
   :channel-type "channel",
   :event-ts "1652772711.926349",
   :inviter "U01FVSUGVN3",
   :subtype "channel_join",
   :team nil,
   :text "<@U01SNLN6L7P> has joined the channel",
   :ts "1652772711.926349",
   :type "message",
   :user "U01SNLN6L7P"})

(def team-joined-event
  {:user
   {:is_email_confirmed true,
    :tz_label "Eastern Daylight Time",
    :deleted false,
    :is_bot false,
    :color "d1707d",
    :real_name "Test User 123",
    :presence "away",
    :updated 1633394736,
    :name "test-user-123",
    :tz "America/New_York",
    :is_primary_owner false,
    :is_restricted false,
    :who_can_share_contact_card "EVERYONE",
    :is_app_user false,
    :is_admin false,
    :is_ultra_restricted false,
    :id "U02GQ1K40JG",
    :team_id "TASMB716H",
    :is_owner false,
    :profile
    {:image_512 "",
     :status_text "",
     :first_name "Test",
     :real_name_normalized "Test User 123",
     :image_48 "",
     :image_192 "",
     :status_expiration 0,
     :real_name "Test User 123",
     :phone "",
     :fields nil,
     :image_72 "",
     :image_24 "",
     :skype "",
     :avatar_hash "g1b3ca723bd9",
     :title "",
     :status_emoji_url "",
     :team "TASMB716H",
     :image_32 "",
     :last_name "User",
     :status_emoji "",
     :display_name "TestUser123",
     :display_name_normalized "TestUser123",
     :status_text_canonical ""},
    :tz_offset -14400},
   :cache_ts 1633394736,
   :event_ts "1633394737.237900",
   :type "team_join"})

(def some-event-types
  "TODO: update if you find more"
  ["bot_added"
   "channel_joined"
   "commands_changed"
   "dnd_updated_user"
   "emoji_changed"
   "file_change"
   "file_deleted"
   "file_public"
   "file_shared"
   "file_unshared"
   "goodbye"
   "group_left"
   "member_joined_channel"
   "member_left_channel"
   "message"
   "pin_added"
   "pin_removed"
   "reaction_added"
   "reaction_removed"
   "team_join"
   "user_change"])
