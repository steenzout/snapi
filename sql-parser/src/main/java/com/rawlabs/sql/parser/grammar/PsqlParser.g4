parser grammar PsqlParser;
options { tokenVocab=PsqlLexer; }
// ============= program =================
prog: code EOF
    ;

code: stmt (SEMICOLON stmt)* SEMICOLON?;

comment: singleline_comment
       | multiline_comment
       ;

singleline_comment: LINE_COMMENT_START singleline_value_comment SL_LINE_COMMENT_END # singleLineComment
                  | LINE_COMMENT_START singleline_value_comment EOF                 # singleLineCommentEOF
                  ;

singleline_value_comment: singleline_param_comment          #singleParamComment
                        | singleline_type_comment           #singleTypeComment
                        | singleline_return_comment         #singleReturnComment
                        | singleline_default_comment        #singleDefaultComment
                        | singleline_unknown_type_comment   #singleUnknownTypeComment
                        | singleline_normal_comment_value   #singleNormalComment
                        ;

// single line comments
singleline_param_comment:  SL_PARAM_KW (SL_WORD)+ (SL_LINE_COMMENT_END LINE_COMMENT_START SL_SPACES singleline_normal_comment_value)*
                    ;

singleline_type_comment: SL_TYPE_KW (SL_WORD)+
                   ;

singleline_default_comment: SL_DEFAULT_KW (SL_WORD)+
                      ;

singleline_return_comment: SL_RETURN_KW (SL_WORD)+  (SL_LINE_COMMENT_END LINE_COMMENT_START SL_SPACES singleline_normal_comment_value)*
                     ;

singleline_unknown_type_comment: SL_UNKNOWN_TOKEN (SL_WORD)*
                           ;

singleline_normal_comment_value: (SL_WORD)*
             ;

// Multiline comments

multiline_comment: MULTI_LINE_COMMENT_START (multiline_value_comment)* MULTI_LINE_COMMENT_END
                 ;

multiline_value_comment: multiline_param_comment            #multilineParamComment
                       | multiline_type_comment             #multilineTypeComment
                       | multiline_default_comment          #multilineDefaultComment
                       | multiline_return_comment           #multilineReturnComment
                       | multiline_unknown_type_comment     #multilineUnknownTypeComment
                       | multiline_normal_comment_value     #multilineNormalComment
                       ;

multiline_param_comment: ML_PARAM_KW (multiline_word_or_star)+
                       ;

multiline_type_comment: ML_TYPE_KW (multiline_word_or_star)+
                      ;

multiline_default_comment: ML_DEFAULT_KW (multiline_word_or_star)+
                         ;

multiline_return_comment: ML_RETURN_KW (multiline_word_or_star)+
                        ;

multiline_unknown_type_comment: ML_UNKNOWN_TOKEN (multiline_word_or_star)*
                              ;

multiline_normal_comment_value: (multiline_word_or_star)+
                      ;

multiline_word_or_star: ML_WORD | ML_STAR;

stmt: L_PAREN stmt R_PAREN   #parenStmt
    | L_SQ_BRACKET stmt R_SQ_BRACKET   #parenStmtSqureBr
    | (stmt_items)+          #stmtItems
    ;

stmt_items: L_PAREN stmt R_PAREN                                                #nestedStmt
          | L_SQ_BRACKET stmt R_SQ_BRACKET                                      #nestedStmtSqureBr
          | stmt_items (COMMA stmt_items)+                                      #commaSeparated
          | proj                                                                #projStmt
          | literal                                                             #literalStmt
          | (reserved_keyword | idnt) L_PAREN (stmt_items)? R_PAREN             #funCallStmt
          | reserved_keyword                                                    #keywordStmt
          | tipe                                                                #typeStmt
          | idnt                                                                #idntStmt
          | PARAM                                                               #paramStmt
          | stmt_items DOUBLE_COLON stmt_items                                  #typeCast
          | operator                                                            #operatorStmt
          | comment                                                             #commentStmt
          | UNKNOWN_WORD IN_UNKNOWN_WORD? (UNKNOWN_WORD_END | EOF)?             #unknownStmt
          ;

reserved_keyword: ABS | ABSENT | ABSOLUTE | ACOS | ACTION | ADD | ALL | ALLOCATE | ALTER | ANALYSE | ANALYZE | ANY | ANY_VALUE | ARE | ARRAY | ARRAY_AGG | ARRAY_MAX_CARDINALITY | AS | ASC | ASENSITIVE | ASIN | ASSERTION | ASYMMETRIC | AT | ATAN | ATOMIC | AUTHORIZATION | AVG | BEGIN | BEGIN_FRAME | BEGIN_PARTITION | BETWEEN | BINARY | BIT_LENGTH | BLOB | BOTH | BTRIM | BY | CALL | CALLED | CARDINALITY | CASCADE | CASCADED | CASE | CAST | CATALOG | CEIL | CEILING | CHARACTER_LENGTH | CHAR_LENGTH | CHECK | CLASSIFIER | CLOB | CLOSE | COALESCE | COLLATE | COLLATION | COLLECT | COLUMN | COMMIT | CONDITION | CONNECT | CONNECTION | CONSTRAINT | CONSTRAINTS | CONTAINS | CONTINUE | CONVERT | COPY | CORR | CORRESPONDING | COS | COSH | COUNT | COVAR_POP | COVAR_SAMP | CREATE | CROSS | CUBE | CUME_DIST | CURRENT | CURRENT_CATALOG | CURRENT_DATE | CURRENT_DEFAULT_TRANSFORM_GROUP | CURRENT_PATH | CURRENT_ROLE | CURRENT_ROW | CURRENT_SCHEMA | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_TRANSFORM_GROUP_FOR_TYPE | CURRENT_USER | CURSOR | CYCLE | DATALINK | DAY | DEALLOCATE | DEC | DECFLOAT | DECLARE | DEFAULT | DEFERRABLE | DEFERRED | DEFINE | DELETE | DENSE_RANK | DEREF | DESC | DESCRIBE | DESCRIPTOR | DETERMINISTIC | DIAGNOSTICS | DISCONNECT | DISTINCT | DLNEWCOPY | DLPREVIOUSCOPY | DLURLCOMPLETE | DLURLCOMPLETEONLY | DLURLCOMPLETEWRITE | DLURLPATH | DLURLPATHONLY | DLURLPATHWRITE | DLURLSCHEME | DLURLSERVER | DLVALUE | DO | DOMAIN | DROP | DYNAMIC | EACH | ELEMENT | ELSE | EMPTY | END | END_EXEC | END_FRAME | END_PARTITION | EQUALS | ESCAPE | EVERY | EXCEPT | EXCEPTION | EXEC | EXECUTE | EXISTS | EXP | EXTERNAL | EXTRACT | FALSE | FETCH | FILTER | FIRST | FIRST_VALUE | FLOAT | FLOOR | FOR | FOREIGN | FOUND | FRAME_ROW | FREE | FROM | FULL | FUNCTION | FUSION | GET | GLOBAL | GO | GOTO | GRANT | GREATEST | GROUP | GROUPING | GROUPS | HAVING | HOLD | HOUR | IDENTITY | IMMEDIATE | IMPORT | IN | INDICATOR | INITIAL | INITIALLY | INNER | INOUT | INPUT | INSENSITIVE | INSERT | INTERSECT | INTERSECTION | INTO | IS | ISOLATION | JOIN | JSON_ARRAY | JSON_ARRAYAGG | JSON_EXISTS | JSON_OBJECT | JSON_OBJECTAGG | JSON_QUERY | JSON_SCALAR | JSON_SERIALIZE | JSON_TABLE | JSON_TABLE_PRIMITIVE | JSON_VALUE | KEY | LAG | LANGUAGE | LARGE | LAST | LAST_VALUE | LATERAL | LEAD | LEADING | LEAST | LEFT | LEVEL | LIKE_REGEX | LISTAGG | LN | LOCAL | LOCALTIME | LOCALTIMESTAMP | LOG | LOG10 | LOWER | LPAD | LTRIM | MATCH | MATCHES | MATCH_NUMBER | MATCH_RECOGNIZE | MAX | MEMBER | MERGE | METHOD | MIN | MINUTE | MOD | MODIFIES | MODULE | MONTH | MULTISET | NAMES | NATIONAL | NATURAL | NCHAR | NCLOB | NEW | NEXT | NO | NONE | NORMALIZE | NTH_VALUE | NTILE | NULL | NULLIF | OCCURRENCES_REGEX | OCTET_LENGTH | OF | OFFSET | OLD | OMIT | ON | ONE | ONLY | OPEN | OPTION | ORDER | OUT | OUTER | OUTPUT | OVER | OVERLAPS | OVERLAY | PAD | PARAMETER | PARTIAL | PARTITION | PATTERN | PER | PERCENT | PERCENTILE_CONT | PERCENTILE_DISC | PERCENT_RANK | PERIOD | PLACING | PORTION | POSITION | POSITION_REGEX | POWER | PRECEDES | PRECISION | PREPARE | PRESERVE | PRIMARY | PRIOR | PRIVILEGES | PROCEDURE | PTF | PUBLIC | RANGE | RANK | READ | READS | RECURSIVE | REF | REFERENCES | REFERENCING | REGR_AVGX | REGR_AVGY | REGR_COUNT | REGR_INTERCEPT | REGR_R2 | REGR_SLOPE | REGR_SXX | REGR_SXY | REGR_SYY | RELATIVE | RELEASE | RESTRICT | RESULT | RETURN | RETURNS | REVOKE | RIGHT | ROLLBACK | ROLLUP | ROW | ROWS | ROW_NUMBER | RPAD | RTRIM | RUNNING | SAVEPOINT | SCHEMA | SCOPE | SCROLL | SEARCH | SECOND | SECTION | SEEK | SELECT | SENSITIVE | SESSION | SESSION_USER | SET | SHOW | SIMILAR | SIN | SINH | SIZE | SKIP_KW | SOME | SPACE | SPECIFIC | SPECIFICTYPE | SQL | SQLCODE | SQLERROR | SQLEXCEPTION | SQLSTATE | SQLWARNING | SQRT | START | STATIC | STDDEV_POP | STDDEV_SAMP | SUBMULTISET | SUBSET | SUBSTRING | SUBSTRING_REGEX | SUCCEEDS | SUM | SYMMETRIC | SYSTEM | SYSTEM_TIME | SYSTEM_USER | TABLE | TABLESAMPLE | TAN | TANH | TEMPORARY | THEN | TIMEZONE_HOUR | TIMEZONE_MINUTE | TO | TRAILING | TRANSACTION | TRANSLATE | TRANSLATE_REGEX | TRANSLATION | TREAT | TRIGGER | TRIM | TRIM_ARRAY | TRUE | TRUNCATE | UESCAPE | UNION | UNIQUE | UNKNOWN | UNNEST | UPDATE | UPPER | USAGE | USER | USING | VALUE | VALUES | VALUE_OF | VARBINARY | VARIADIC | VARYING | VAR_POP | VAR_SAMP | VERSIONING | VIEW | WHEN | WHENEVER | WHERE | WIDTH_BUCKET | WINDOW | WITH | WITHIN | WITHOUT | WORK | WRITE | XMLAGG | XMLATTRIBUTES | XMLBINARY | XMLCAST | XMLCOMMENT | XMLCONCAT | XMLDOCUMENT | XMLELEMENT | XMLEXISTS | XMLFOREST | XMLITERATE | XMLNAMESPACES | XMLPARSE | XMLPI | XMLQUERY | XMLSERIALIZE | XMLTABLE | XMLTEXT | XMLVALIDATE | YEAR | ZONE | STAR | UNICODE ;
non_reserved_keyword: A_KW | ABORT | ACCESS | ACCORDING | ADA | ADMIN | AFTER | AGGREGATE | ALSO | ALWAYS | ASSIGNMENT | ATTACH | ATTRIBUTE | ATTRIBUTES | BACKWARD | BASE64 | BEFORE | BERNOULLI | BLOCKED | BOM | BREADTH | C_KW | CACHE | CATALOG_NAME | CHAIN | CHAINING | CHARACTERISTICS | CHARACTERS | CHARACTER_SET_CATALOG | CHARACTER_SET_NAME | CHARACTER_SET_SCHEMA | CHECKPOINT | CLASS | CLASS_ORIGIN | CLUSTER | COBOL | COLLATION_CATALOG | COLLATION_NAME | COLLATION_SCHEMA | COLUMNS | COLUMN_NAME | COMMAND_FUNCTION | COMMAND_FUNCTION_CODE | COMMENT | COMMENTS | COMMITTED | COMPRESSION | CONCURRENTLY | CONDITIONAL | CONDITION_NUMBER | CONFIGURATION | CONFLICT | CONNECTION_NAME | CONSTRAINT_CATALOG | CONSTRAINT_NAME | CONSTRAINT_SCHEMA | CONSTRUCTOR | CONTENT | CONTROL | CONVERSION | COPARTITION | COST | CSV | CURSOR_NAME | DATA | DATABASE | DATETIME_INTERVAL_CODE | DATETIME_INTERVAL_PRECISION | DB | DEFAULTS | DEFINED | DEFINER | DEGREE | DELIMITER | DELIMITERS | DEPENDS | DEPTH | DERIVED | DETACH | DICTIONARY | DISABLE | DISCARD | DISPATCH | DOCUMENT | DYNAMIC_FUNCTION | DYNAMIC_FUNCTION_CODE | ENABLE | ENCODING | ENCRYPTED | ENFORCED | ENUM | ERROR | EVENT | EXCLUDE | EXCLUDING | EXCLUSIVE | EXPLAIN | EXPRESSION | EXTENSION | FAMILY | FILE | FINAL | FINALIZE | FINISH | FLAG | FOLLOWING | FORCE | FORMAT | FORTRAN | FORWARD | FREEZE | FS | FULFILL | FUNCTIONS | G_KW | GENERAL | GENERATED | GRANTED | HANDLER | HEADER | HEX | HIERARCHY | ID | IF | IGNORE | IMMEDIATELY | IMMUTABLE | IMPLEMENTATION | IMPLICIT | INCLUDE | INCLUDING | INCREMENT | INDENT | INDEX | INDEXES | INHERIT | INHERITS | INLINE | INSTANCE | INSTANTIABLE | INSTEAD | INTEGRITY | INVOKER | ISNULL | K_KW | KEEP | KEYS | KEY_MEMBER | KEY_TYPE | LABEL | LEAKPROOF | LENGTH | LIBRARY | LIMIT | LINK | LISTEN | LOAD | LOCATION | LOCATOR | LOCK | LOCKED | LOGGED | M_KW | MAP | MAPPING | MATCHED | MATERIALIZED | MAXVALUE | MEASURES | MESSAGE_LENGTH | MESSAGE_OCTET_LENGTH | MESSAGE_TEXT | MINVALUE | MODE | MORE_KW | MOVE | MUMPS | NAME | NAMESPACE | NESTED | NESTING | NFC | NFD | NFKC | NFKD | NIL | NORMALIZED | NOTHING | NOTIFY | NOTNULL | NOWAIT | NULLABLE | NULLS | NULL_ORDERING | NUMBER | OBJECT | OCCURRENCE | OCTETS | OFF | OIDS | OPERATOR_KW | OPTIONS | ORDERING | ORDINALITY | OTHERS | OVERFLOW | OVERRIDING | OWNED | OWNER | P_KW | PARALLEL | PARAMETER_MODE | PARAMETER_NAME | PARAMETER_ORDINAL_POSITION | PARAMETER_SPECIFIC_CATALOG | PARAMETER_SPECIFIC_NAME | PARAMETER_SPECIFIC_SCHEMA | PARSER | PASCAL | PASS | PASSING | PASSTHROUGH | PASSWORD | PAST | PERMISSION | PERMUTE | PIPE | PLAN | PLANS | PLI | POLICY | PRECEDING | PREPARED | PREV | PRIVATE | PROCEDURAL | PROCEDURES | PROGRAM | PRUNE | PUBLICATION | QUOTES | REASSIGN | RECHECK | RECOVERY | REFRESH | REINDEX | RENAME | REPEATABLE | REPLACE | REPLICA | REQUIRING | RESET | RESPECT | RESTART | RESTORE | RETURNED_CARDINALITY | RETURNED_LENGTH | RETURNED_OCTET_LENGTH | RETURNED_SQLSTATE | RETURNING | ROLE | ROUTINE | ROUTINES | ROUTINE_CATALOG | ROUTINE_NAME | ROUTINE_SCHEMA | ROW_COUNT | RULE | SCALAR | SCALE | SCHEMAS | SCHEMA_NAME | SCOPE_CATALOG | SCOPE_NAME | SCOPE_SCHEMA | SECURITY | SELECTIVE | SELF | SEMANTICS | SEQUENCE | SEQUENCES | SERIALIZABLE | SERVER | SERVER_NAME | SETOF | SETS | SHARE | SIMPLE | SNAPSHOT | SORT_DIRECTION | SOURCE | SPECIFIC_NAME | STABLE | STANDALONE | STATE | STATEMENT | STATISTICS | STDIN | STDOUT | STORAGE | STORED | STRICT | STRING | STRIP | STRUCTURE | STYLE | SUBCLASS_ORIGIN | SUBSCRIPTION | SUPPORT | SYSID | T_KW | TABLES | TABLESPACE | TABLE_NAME | TEMP | TEMPLATE | THROUGH | TIES | TOKEN | TOP_LEVEL_COUNT | TRANSACTIONS_COMMITTED | TRANSACTIONS_ROLLED_BACK | TRANSACTION_ACTIVE | TRANSFORM | TRANSFORMS | TRIGGER_CATALOG | TRIGGER_NAME | TRIGGER_SCHEMA | TRUSTED | TYPE | TYPES | UNBOUNDED | UNCOMMITTED | UNCONDITIONAL | UNDER | UNENCRYPTED | UNLINK | UNLISTEN | UNLOGGED | UNMATCHED | UNNAMED | UNTIL | UNTYPED | URI | USER_DEFINED_TYPE_CATALOG | USER_DEFINED_TYPE_CODE | USER_DEFINED_TYPE_NAME | USER_DEFINED_TYPE_SCHEMA | UTF16 | UTF32 | UTF8 | VACUUM | VALID | VALIDATE | VALIDATOR | VERBOSE | VERSION | VIEWS | VOLATILE | WHITESPACE | WRAPPER | XMLDECLARATION | XMLROOT | XMLSCHEMA | YES ;

operator: EQ | LE | GT | LEQ | GEQ | NEQ1 | NEQ2 | LIKE | ILIKE | AND | OR | NOT | STAR | PLUS | MINUS | PLUS | DIV | MOD_OP | EXP_OP | SQ_ROOT | CUBE_ROOT | FACTORIAL | FACTORIAS | PLUS | BITWISE_AND | BITWISE_OR | BITWISE_XOR | BITWISE_NOT | BITWISE_SHIFT_LEFT | BITWISE_SHIFT_RIGHT | CONCAT | REGEX_CASE_INSENSITIVE_MATCH | REGEX_CASE_INSENSITIVE_NOT_MATCH;

proj: idnt (DOT idnt)+                                                        #properProj
    | idnt DOT {notifyErrorListeners("Missing identifier after '.'");}        #missingIdenProj
    ;

idnt: (WORD | STRING_IDENTIFIER_START (STRING_IDENTIFIER_CONTENT | STRING_ESCAPE)* STRING_IDENTIFIER_END? | non_reserved_keyword);


literal: STRING_LITERAL_START STRING_LITERAL_CONTENT* STRING_LITERAL_END #stringLiteral
       | NO_FLOATING_NUMBER                                              #integerLiteral
       | FLOATING_POINT                                                  #floatingPointLiteral
       | (TRUE | FALSE)                                                  #booleanLiteral
       ;

tipe: psql_type (L_PAREN (NO_FLOATING_NUMBER (COMMA NO_FLOATING_NUMBER)?) R_PAREN)? WITH_TIME_ZONE?;

psql_type: BIGINT | BIGSERIAL | BIT | BIT_VARYING | BOOLEAN | BOX | BYTEA | CHARACTER | CHARACTER_VARYING | VARCHAR | CIDR | CIRCLE | DATE | DOUBLE | INET | INTEGER | INTERVAL | JSON | JSONB | LINE | LSEG | MACADDR | MACADDR8 | MONEY | NUMERIC | PATH | PG_LSN | PG_SNAPSHOT | POINT | POLYGON | REAL | SMALLINT | SMALLSERIAL | SERIAL | TEXT | TIME | TIMESTAMP | TSQUERY | TSVECTOR | TXID_SNAPSHOT | UUID | XML | INT_8 | SERIAL_8 | VARBIT | BOOL | CHAR | FLOAT_8 | INT_4 | INT | DECIMAL | FLOAT_4 | INT_2 | SERIAL_2 | SERIAL_4 | TIMESTAMPTZ;