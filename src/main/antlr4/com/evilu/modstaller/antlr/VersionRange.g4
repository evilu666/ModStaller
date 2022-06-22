grammar VersionRange;

r: exp EOF;

exp: mavenRange | prefixRange | exactVersion;


mavenRange: exactRange | boundRange;
exactRange: INCLUSIVE_BOUND_START exactVersion INCLUSIVE_BOUND_END;
boundRange returns [int g]: (INCLUSIVE_BOUND_START | EXCLUSIVE_BOUND_START | EXCLUSIVE_BOUND_END) (exactVersion {$g=1;})? ',' WS* (exactVersion {$g=2;})? (INCLUSIVE_BOUND_END | EXCLUSIVE_BOUND_END | EXCLUSIVE_BOUND_START);


prefixRange: prefixRangeWithMcAndForge | prefixRangeWithMc | prefixVersion;

prefixRangeWithMcAndForge: prefixVersion VERSION_SEP prefixVersion VERSION_SEP prefixVersion;
prefixRangeWithMc: prefixVersion VERSION_SEP prefixVersion;
prefixVersion: VERSION_PART (DOT VERSION_PART)*? DOT ('*' | '+');


exactVersion: exactVersionWithMcAndForge | exactVersionWithMc | version;

exactVersionWithMcAndForge: version '-' version '-' version;
exactVersionWithMc: (version '-' version) | ('mc' version '_v' version);
version: VERSION_PART (DOT VERSION_PART)*?;

VERSION_SEP: '-';



VERSION_PART: [0-9]+;
INCLUSIVE_BOUND_START: '[';
INCLUSIVE_BOUND_END: ']';
EXCLUSIVE_BOUND_START: '(';
EXCLUSIVE_BOUND_END: ')';
DOT: '.';
WS : [ \t\r\n] -> skip;
