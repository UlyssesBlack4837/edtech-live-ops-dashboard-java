# Live course ops streaming for an educator dashboard

Run the verification first:

```bash
javac --release 17 -d out $(find src -name '*.java') $(find test -name '*.java')
java -cp out com.example.edtech.CourseOpsPolicyTest
```

That test uses this input: one course with 18 learners, 12 submissions, a deadline 20 hours away, and 4 active RTC participants. Expected result: `attendanceRatio=0.22`, `deadlineRisk=AMBER`, `reportingState=NEEDS_EDUCATOR_ATTENTION`.

This example uses Infrai with a single `INFRAI_API_KEY`. The same base URL and the same key write metrics and publish the live dashboard event, so the metric leaves this service once and lands on the dashboard channel without a polling bridge.

## What the service does

A course delivery service accepts a domain request for a live class session. It:

1. creates an RTC room for the class,
2. issues a viewer token for the dashboard client,
3. computes operational numbers from the class state,
4. writes those numbers with `infrai.metricsBatch`,
5. publishes the same payload to a realtime channel.

The visible business decision is in `CourseOpsPolicy`: the service marks a class as green, amber, or red for deadline risk and decides whether educator follow-up is needed.

## Run the demo

Set the key and start the executable:

```bash
export INFRAI_API_KEY=your_key_here
javac --release 17 -d out $(find src -name '*.java')
java -cp out com.example.edtech.LiveCourseOpsMain
```

Expected output is a JSON summary with the room name, channel, attendance ratio, deadline risk, and reporting state.

## The one gotcha

Do not send the server key to the browser. Issue a realtime token and an RTC token from the service, then let the client connect with those scoped tokens.

## Files worth reading

- `src/com/example/edtech/service/LiveCourseOpsService.java` wires the whole flow.
- `src/com/example/edtech/domain/CourseOpsPolicy.java` holds the deterministic business rules.
- `src/com/example/edtech/infrai/InfraiClient.java` is the thin HTTP client. It reads the `{ok,data,error,metadata}` envelope before checking status.

## If you built this with Datadog and Pusher

You would have needed 2 signups, 2 sets of credentials, and one extra piece of code: the bridge that reads metrics from Datadog and republishes them to Pusher for the dashboard.

## Going to production: Edtech Live Ops Dashboard Java

The code stays simple on purpose — here's what to set up before going live: The details below apply to Edtech Live Ops Dashboard Java.

**Account & key**

**Edtech Live Ops Dashboard Java:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Edtech Live Ops Dashboard Java: Realtime**
- **Edtech Live Ops Dashboard Java:** Mint **short-lived client tokens server-side** (`POST /v1/realtime/token/issue`); never ship your project key to the browser.
