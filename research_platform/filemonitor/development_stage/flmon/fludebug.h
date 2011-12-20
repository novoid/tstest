
#ifndef __FLUDEBUG_H__
#define __FLUDEBUG_H__

#include <stdio.h>


#ifdef DEBUG

#define DBGUPRINT(_s_) printf(_s_)
#define DBGUPRINT1(_s_, _a_) printf(_s_, _a_)
#define DBGUPRINT2(_s_, _a1_, _a2_) printf(_s_, _a1_, _a2_)
#define DBGUPRINT3(_s_, _a1_, _a2_, _a3_) printf(_s_, _a1_, _a2_, _a3_)

#else

#define DBGUPRINT(_s_)
#define DBGUPRINT1(_s_, _a_)
#define DBGUPRINT2(_s_, _a1_, _a2_)
#define DBGUPRINT3(_s_, _a1_, _a2_, _a3_)


#endif

#endif