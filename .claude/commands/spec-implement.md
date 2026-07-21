---
description: Implement an approved task list one task at a time, checking off progress as it goes.
---
Given a spec slug:

1. Read `specs/<slug>/tasks.md`. If Status is not `Approved`, stop and ask
   the user to approve it first.
2. Work through unchecked tasks in order. For each: implement it, add or
   update tests covering the behavior this task introduces or changes
   (per CLAUDE.md's testing rule, running the pre-existing suite alone is
   not enough), run the full build/test suite, then mark it `- [x]` in
   tasks.md only once everything passes and the new behavior is actually
   covered.
3. If a task turns out to need a design decision not covered by plan.md,
   stop and ask rather than improvising, and suggest updating plan.md.
4. When all tasks are checked, summarize what was built and note anything
   that deviated from the plan.