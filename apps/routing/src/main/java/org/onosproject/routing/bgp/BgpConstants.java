/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.routing.bgp;

/**
 * BGP related constants. BGP相关常量
 */
public final class BgpConstants {
    /**
     * Default constructor.
     * <p>
     * The constructor is private to prevent creating an instance of
     * this utility class.
     */
    private BgpConstants() {
    }

    /** BGP port number (RFC 4271). */
    public static final int BGP_PORT = 179;

    /** BGP version. */
    public static final int BGP_VERSION = 4;

    /** BGP OPEN message type. */
    public static final int BGP_TYPE_OPEN = 1;

    /** BGP UPDATE message type. */
    public static final int BGP_TYPE_UPDATE = 2;

    /** BGP NOTIFICATION message type. */
    public static final int BGP_TYPE_NOTIFICATION = 3;

    /** BGP KEEPALIVE message type. */
    public static final int BGP_TYPE_KEEPALIVE = 4;

    /** BGP Header Marker field length. */
    public static final int BGP_HEADER_MARKER_LENGTH = 16;

    /** BGP Header length. */
    public static final int BGP_HEADER_LENGTH = 19;

    /** BGP message maximum length. */
    public static final int BGP_MESSAGE_MAX_LENGTH = 4096;

    /** BGP OPEN message minimum length (BGP Header included). */
    public static final int BGP_OPEN_MIN_LENGTH = 29;

    /** BGP UPDATE message minimum length (BGP Header included). */
    public static final int BGP_UPDATE_MIN_LENGTH = 23;

    /** BGP NOTIFICATION message minimum length (BGP Header included). */
    public static final int BGP_NOTIFICATION_MIN_LENGTH = 21;

    /** BGP KEEPALIVE message expected length (BGP Header included). */
    public static final int BGP_KEEPALIVE_EXPECTED_LENGTH = 19;

    /** BGP KEEPALIVE messages transmitted per Hold interval. */
    public static final int BGP_KEEPALIVE_PER_HOLD_INTERVAL = 3;

    /** BGP KEEPALIVE messages minimum Holdtime (in seconds). */
    public static final int BGP_KEEPALIVE_MIN_HOLDTIME = 3;

    /** BGP KEEPALIVE messages minimum transmission interval (in seconds). */
    public static final int BGP_KEEPALIVE_MIN_INTERVAL = 1;

    /** BGP AS 0 (zero) value. See draft-ietf-idr-as0-06.txt Internet Draft. */
    public static final long BGP_AS_0 = 0;

    /**
     * BGP OPEN related constants.
     */
    public static final class Open {
        /**
         * Default constructor.
         * <p>
         * The constructor is private to prevent creating an instance of
         * this utility class.
         */
        private Open() {
        }

        /**
         * BGP OPEN: Optional Parameters related constants.
         */
        public static final class OptionalParameters {
        }

        /**
         * BGP OPEN: Capabilities related constants (RFC 5492).
         */
        /* RFC3392/5492定义了可协商参数
         *     设计初衷： rfc4271规定，BGP speaker如果在OPEN消息中遇到不认知的可选参数，那BGP speaker必须
         *               关闭对等连接；此行为使得引入新特性变得异常复杂；因此，本rfc引入了能力协商选项，允许
         *               BGP speaker在OPEN消息中协商双方的能力，仅双发都支持的能力才能被认可，其他的(包括
         *               不认知的)都被忽略。
         *     交互流程： 支持某特性的BGP speaker发送OPEN信息时，可能携带此可选项；
         *               接收方，逐一检测选项列表特性，可得知对端支持的可选特性；
         *               当得知对方支持某特性后，如果本地也支持，则后续通信可采用此特性；
         *               如果双方任何一方不支持某特性，则后续通信不能采用此特性；
         *
         *               如果接收方不支持此协商特性，发送方会接收到NOTIFY信息，以提示不认知的属性信息；为
         *               继续建立对等体关系，发送方必须重新发送OPEN消息，并且抛弃协商参数。
         *
         *               如果BGP speaker发现对端不支持自身的某些必须特性，可以发送NOTIFY信息，以断开连接；
         *               并且后续不再试图自动重新建立对等体连接。
         *
         *               如果BGP speaker收到了不支持的协商特性，它必须忽略此特性；而不是断开连接
         *
         *     可协商参数类型为2
         *               1) 一般后续跟随1个或多个特性
         *               2) 一般只有一个可协商参数
         *               3) 但也要做好兼容老协议的准备，多个参数，每个参数1个或多个特性
         */
        public static final class Capabilities {
            /** BGP OPEN Optional Parameter Type: Capabilities. */
            public static final int TYPE = 2;

            /** BGP OPEN Optional Parameter minimum length. */
            public static final int MIN_LENGTH = 2;

            /**
             * BGP OPEN: Multiprotocol Extensions Capabilities (RFC 4760).
             */
            /* RFC4760定义了此特性： 此属性用于拓展BGP-4，使得其可以携带multiple Network Layer protocols
             * (e.g., IPv6, IPX, L3VPN, etc.)等路由信息； 此属性为后向兼容，支持此特性的BGP speaker
             * 可以和不支持此特性的BGP spearker建立对等体关系
             *
             * BGP-4中，带有IPv4特性的三个属性为：
             *      NEXT_HOP
             *      AGGREGATOR
             *      NLRI
             * 相关的RFC假设BGP speaker必须拥有一个IPv4地址，以标识特定属性，如AGGREGATOR；因此，支持MNLP
             * 只需要在现有BGP-4中添加两点：
             *     1) the ability to associate a particular Network Layer protocol with the next hop information
             *     2) the ability to associate a particular Network Layer protocol with NLRI
             *
             * 另外，下一跳信息一般是和建议的可达地址关联的，而和非可达地址没有关联；因此，应该把下一跳信息和建议可达地址组合
             * 起来对待，而分开建议可达信息与非可达信息。
             *
             * 为了后向兼容，并且简化引入multiprotocol能力的过程，此RFC建议添加两种新属性：
             *     1) Multiprotocol Reachable NLRI(MP_REACH_NLRI)： 携带建议可达信息及下一跳信息
             *     2) Multiprotocol Unreachable NLRI (MP_UNREACH_NLRI)：携带建议不可达信息
             * 这两个属性均为“可选非传递属性”（optional and non-transitive），不支持这些属性的BGP speaker将忽略此属性，
             * 并且不传递出去。
             *
             * 属性MP_REACH_NLRI格式如下：
             * +---------------------------------------------------------+
             * | Address Family Identifier (2 octets) |
             * +---------------------------------------------------------+
             * | Subsequent Address Family Identifier (1 octet) |
             * +---------------------------------------------------------+
             * | Length of Next Hop Network Address (1 octet) |
             * +---------------------------------------------------------+
             * | Network Address of Next Hop (variable) |
             * +---------------------------------------------------------+
             * | Reserved (1 octet) |
             * +---------------------------------------------------------+
             * | Network Layer Reachability Information (variable) |
             * +---------------------------------------------------------+
             *
             * 属性MP_UNREACH_NLRI格式如下：
             * +---------------------------------------------------------+
             * | Address Family Identifier (2 octets) |
             * +---------------------------------------------------------+
             * | Subsequent Address Family Identifier (1 octet) |
             * +---------------------------------------------------------+
             * | Withdrawn Routes (variable) |
             * +---------------------------------------------------------+
             *
             * Network Layer Reachability information如下编码：
             * +---------------------------+
             * | Length (1 octet) |
             * +---------------------------+
             * | Prefix (variable) |
             * +---------------------------+
             *
             * Subsequent Address Family Identifier：
             *     1 - Network Layer Reachability Information used for unicast forwarding
             *     2 - Network Layer Reachability Information used for multicast forwarding
             *
             * 在OPEN消息中协商这些属性的Capabilities Optional Parameter属性列表格式为：
             * 0       7       15     23      31
             * +-------+-------+-------+-------+
             * |    AFI      | Res.    | SAFI |
             * +-------+-------+-------+-------+
             *      AFI  - Address Family Identifier（16bit）
             *      Res. - Reserved (8 bit) field
             *      SAFI - Subsequent Address Family Identifier (8 bit)
             */
            public static final class MultiprotocolExtensions {
                /** BGP OPEN Multiprotocol Extensions code. */
                public static final int CODE = 1;

                /** BGP OPEN Multiprotocol Extensions length. */
                public static final int LENGTH = 4;

                /** BGP OPEN Multiprotocol Extensions AFI: IPv4. */
                public static final int AFI_IPV4 = 1;

                /** BGP OPEN Multiprotocol Extensions AFI: IPv6. */
                public static final int AFI_IPV6 = 2;

                /** BGP OPEN Multiprotocol Extensions SAFI: unicast. */
                public static final int SAFI_UNICAST = 1;

                /** BGP OPEN Multiprotocol Extensions SAFI: multicast. */
                public static final int SAFI_MULTICAST = 2;
            }

            /**
             * BGP OPEN: Support for 4-octet AS Number Capability (RFC 6793).
             */
            /**
             * BGP-4中的AS号是16bit位的；RFC6793介绍了一种拓展属性，使得AS号可以利用32bit位表示；
             * 目的是防止AS号迅速耗尽。
             *
             * 需要携带AS号的属性包括：
             *      OPEN消息的"My Autonomous System" field
             *      UPDATE消息的AS_PATH + AGGREGATOR属性字段
             *      BGP Communities属性
             *
             * 本RFC通过在OPEN消息中的Capabilities Optional Parameter参数的属性列表中协商是否支持此能力
             *      code: 65
             *
             * 支持此能力后，将引入了两个新属性(可选传递属性，optional transitive)，来传递4字节的AS号：
             *      AS4_PATH            ===     拓展原AS_PATH              ===     17
             *      AS4_AGGREGATOR      ===     拓展原AS_AGGREGATOR        ===     18
             *
             * 当前，被配置赋值的2字节AS号通过设置高2字节为0转换为4字节的AS号；此4字节的AS号称为be mappable to
             * a two-octet AS number；
             *
             * AS_TRANS：当4字节的AS号不能转换为2字节时，本RFC预留了一个2字节的AS号来表示这种情况，称为AS_TRANS(23456）;
             *           以便利用它编码必须使用2字节AS号的地方，如OPEN消息的"My Autonomous System" field
             *
             * <NOTE>
             *     1) 协商支持此能力后，AS号必须使用能力协商值，替换掉记录下来的OPEN消息中的"My Autonomous System" field
             *     2) 都支持此能力的BGP speaker之间，在UPDATE消息中必须使用AS_PATH/AS_AGGREGATOR传递，而不是AS4_PATH/AS4_AGGREGATOR
             */
            public static final class As4Octet {
                /** BGP OPEN Support for 4-octet AS Number Capability code. */
                public static final int CODE = 65;

                /** BGP OPEN 4-octet AS Number Capability length. */
                public static final int LENGTH = 4;
            }
        }
    }

    /**
     * BGP UPDATE related constants.
     */
    public static final class Update {
        /**
         * Default constructor.
         * <p>
         * The constructor is private to prevent creating an instance of
         * this utility class.
         */
        private Update() {
        }

        /** BGP AS length. */
        public static final int AS_LENGTH = 2;

        /** BGP 4 Octet AS length (RFC 6793). */
        public static final int AS_4OCTET_LENGTH = 4;

        /**
         * BGP UPDATE: ORIGIN related constants.
         */
        public static final class Origin {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private Origin() {
            }

            /** BGP UPDATE Attributes Type Code ORIGIN. */
            public static final int TYPE = 1;

            /** BGP UPDATE Attributes Type Code ORIGIN length. */
            public static final int LENGTH = 1;

            /** BGP UPDATE ORIGIN: IGP. */
            public static final int IGP = 0;

            /** BGP UPDATE ORIGIN: EGP. */
            public static final int EGP = 1;

            /** BGP UPDATE ORIGIN: INCOMPLETE. */
            public static final int INCOMPLETE = 2;

            /**
             * Gets the BGP UPDATE origin type as a string.
             *
             * @param type the BGP UPDATE origin type
             * @return the BGP UPDATE origin type as a string
             */
            public static String typeToString(int type) {
                String typeString = "UNKNOWN";

                switch (type) {
                case IGP:
                    typeString = "IGP";
                    break;
                case EGP:
                    typeString = "EGP";
                    break;
                case INCOMPLETE:
                    typeString = "INCOMPLETE";
                    break;
                default:
                    break;
                }
                return typeString;
            }
        }

        /**
         * BGP UPDATE: AS_PATH related constants.
         */
        public static final class AsPath {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private AsPath() {
            }

            /** BGP UPDATE Attributes Type Code AS_PATH. */
            public static final int TYPE = 2;

            /** BGP UPDATE AS_PATH Type: AS_SET. */
            public static final int AS_SET = 1;

            /** BGP UPDATE AS_PATH Type: AS_SEQUENCE. */
            public static final int AS_SEQUENCE = 2;

            /** BGP UPDATE AS_PATH Type: AS_CONFED_SEQUENCE. */
            public static final int AS_CONFED_SEQUENCE = 3;

            /** BGP UPDATE AS_PATH Type: AS_CONFED_SET. */
            public static final int AS_CONFED_SET = 4;

            /**
             * Gets the BGP AS_PATH type as a string.
             *
             * @param type the BGP AS_PATH type
             * @return the BGP AS_PATH type as a string
             */
            public static String typeToString(int type) {
                String typeString = "UNKNOWN";

                switch (type) {
                case AS_SET:
                    typeString = "AS_SET";
                    break;
                case AS_SEQUENCE:
                    typeString = "AS_SEQUENCE";
                    break;
                case AS_CONFED_SEQUENCE:
                    typeString = "AS_CONFED_SEQUENCE";
                    break;
                case AS_CONFED_SET:
                    typeString = "AS_CONFED_SET";
                    break;
                default:
                    break;
                }
                return typeString;
            }
        }

        /**
         * BGP UPDATE: NEXT_HOP related constants.
         */
        public static final class NextHop {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private NextHop() {
            }

            /** BGP UPDATE Attributes Type Code NEXT_HOP. */
            public static final int TYPE = 3;

            /** BGP UPDATE Attributes Type Code NEXT_HOP length. */
            public static final int LENGTH = 4;
        }

        /**
         * BGP UPDATE: MULTI_EXIT_DISC related constants.
         */
        public static final class MultiExitDisc {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private MultiExitDisc() {
            }

            /** BGP UPDATE Attributes Type Code MULTI_EXIT_DISC. */
            public static final int TYPE = 4;

            /** BGP UPDATE Attributes Type Code MULTI_EXIT_DISC length. */
            public static final int LENGTH = 4;

            /** BGP UPDATE Attributes lowest MULTI_EXIT_DISC value. */
            public static final int LOWEST_MULTI_EXIT_DISC = 0;
        }

        /**
         * BGP UPDATE: LOCAL_PREF related constants.
         */
        public static final class LocalPref {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private LocalPref() {
            }

            /** BGP UPDATE Attributes Type Code LOCAL_PREF. */
            public static final int TYPE = 5;

            /** BGP UPDATE Attributes Type Code LOCAL_PREF length. */
            public static final int LENGTH = 4;
        }

        /**
         * BGP UPDATE: ATOMIC_AGGREGATE related constants.
         */
        public static final class AtomicAggregate {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private AtomicAggregate() {
            }

            /** BGP UPDATE Attributes Type Code ATOMIC_AGGREGATE. */
            public static final int TYPE = 6;

            /** BGP UPDATE Attributes Type Code ATOMIC_AGGREGATE length. */
            public static final int LENGTH = 0;
        }

        /**
         * BGP UPDATE: AGGREGATOR related constants.
         */
        public static final class Aggregator {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private Aggregator() {
            }

            /** BGP UPDATE Attributes Type Code AGGREGATOR. */
            public static final int TYPE = 7;

            /** BGP UPDATE Attributes Type Code AGGREGATOR length: 2 octet AS. */
            public static final int AS2_LENGTH = 6;

            /** BGP UPDATE Attributes Type Code AGGREGATOR length: 4 octet AS. */
            public static final int AS4_LENGTH = 8;
        }

        /**
         * BGP UPDATE: MP_REACH_NLRI related constants.
         */
        public static final class MpReachNlri {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private MpReachNlri() {
            }

            /** BGP UPDATE Attributes Type Code MP_REACH_NLRI. */
            public static final int TYPE = 14;

            /** BGP UPDATE Attributes Type Code MP_REACH_NLRI min length. */
            public static final int MIN_LENGTH = 5;
        }

        /**
         * BGP UPDATE: MP_UNREACH_NLRI related constants.
         */
        public static final class MpUnreachNlri {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private MpUnreachNlri() {
            }

            /** BGP UPDATE Attributes Type Code MP_UNREACH_NLRI. */
            public static final int TYPE = 15;

            /** BGP UPDATE Attributes Type Code MP_UNREACH_NLRI min length. */
            public static final int MIN_LENGTH = 3;
        }
    }

    /**
     * BGP NOTIFICATION related constants.
     */
    public static final class Notifications {
        /**
         * Default constructor.
         * <p>
         * The constructor is private to prevent creating an instance of
         * this utility class.
         */
        private Notifications() {
        }

        /**
         * BGP NOTIFICATION: Message Header Error constants.
         */
        public static final class MessageHeaderError {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private MessageHeaderError() {
            }

            /** Message Header Error code. */
            public static final int ERROR_CODE = 1;

            /** Message Header Error subcode: Connection Not Synchronized. */
            public static final int CONNECTION_NOT_SYNCHRONIZED = 1;

            /** Message Header Error subcode: Bad Message Length. */
            public static final int BAD_MESSAGE_LENGTH = 2;

            /** Message Header Error subcode: Bad Message Type. */
            public static final int BAD_MESSAGE_TYPE = 3;
        }

        /**
         * BGP NOTIFICATION: OPEN Message Error constants.
         */
        public static final class OpenMessageError {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private OpenMessageError() {
            }

            /** OPEN Message Error code. */
            public static final int ERROR_CODE = 2;

            /** OPEN Message Error subcode: Unsupported Version Number. */
            public static final int UNSUPPORTED_VERSION_NUMBER = 1;

            /** OPEN Message Error subcode: Bad PEER AS. */
            public static final int BAD_PEER_AS = 2;

            /** OPEN Message Error subcode: Unacceptable Hold Time. */
            public static final int UNACCEPTABLE_HOLD_TIME = 6;
        }

        /**
         * BGP NOTIFICATION: UPDATE Message Error constants.
         */
        public static final class UpdateMessageError {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private UpdateMessageError() {
            }

            /** UPDATE Message Error code. */
            public static final int ERROR_CODE = 3;

            /** UPDATE Message Error subcode: Malformed Attribute List. */
            public static final int MALFORMED_ATTRIBUTE_LIST = 1;

            /** UPDATE Message Error subcode: Unrecognized Well-known Attribute. */
            public static final int UNRECOGNIZED_WELL_KNOWN_ATTRIBUTE = 2;

            /** UPDATE Message Error subcode: Missing Well-known Attribute. */
            public static final int MISSING_WELL_KNOWN_ATTRIBUTE = 3;

           /** UPDATE Message Error subcode: Attribute Flags Error. */
            public static final int ATTRIBUTE_FLAGS_ERROR = 4;

            /** UPDATE Message Error subcode: Attribute Length Error. */
            public static final int ATTRIBUTE_LENGTH_ERROR = 5;

            /** UPDATE Message Error subcode: Invalid ORIGIN Attribute. */
            public static final int INVALID_ORIGIN_ATTRIBUTE = 6;

            /** UPDATE Message Error subcode: Invalid NEXT_HOP Attribute. */
            public static final int INVALID_NEXT_HOP_ATTRIBUTE = 8;

            /** UPDATE Message Error subcode: Optional Attribute Error. Unused. */
            public static final int OPTIONAL_ATTRIBUTE_ERROR = 9;

            /** UPDATE Message Error subcode: Invalid Network Field. */
            public static final int INVALID_NETWORK_FIELD = 10;

            /** UPDATE Message Error subcode: Malformed AS_PATH. */
            public static final int MALFORMED_AS_PATH = 11;
        }

        /**
         * BGP NOTIFICATION: Hold Timer Expired constants.
         */
        public static final class HoldTimerExpired {
            /**
             * Default constructor.
             * <p>
             * The constructor is private to prevent creating an instance of
             * this utility class.
             */
            private HoldTimerExpired() {
            }

            /** Hold Timer Expired code. */
            public static final int ERROR_CODE = 4;
        }

        /** BGP NOTIFICATION message Error subcode: Unspecific. */
        public static final int ERROR_SUBCODE_UNSPECIFIC = 0;
    }
}
