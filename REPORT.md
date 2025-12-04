# Lab 6 Microservices - Project Report

**Author:** Ariana Porroche Llorén (874055)

**Date:** 04th December 2025

**Course:** Web Engineering


## 1. Configuration Setup

- **Modifications in `accounts-service.yml`**
  ```
  # HTTP Server
  server:
    port: 2222
  ```
- **Why is externalized configuration useful in microservices?**

  Externalized configuration is useful because it allows microservices to load their settings from an external source instead of hardcoding them. This makes it easy to update configuration (like ports or URLs) without redeploying the service, ensures consistent settings across multiple instances, and allows different configurations per environment.

---

## 2. Service Registration (Task 1)

### Accounts Service Registration

![Accounts Registration Log](docs/screenshots/accounts-registration.png)

```
2025-12-04T19:47:41.837+01:00  INFO 7722 --- [accounts-service] [foReplicator-%d] com.netflix.discovery.DiscoveryClient    : DiscoveryClient_ACCOUNTS-SERVICE/localhost:accounts-service:3333 - registration status: 204
```

- **What happens during service registration?**

1. It first loads its configuration from the Config Server and initializes its web server and database.
2. Once the application is fully up, the Eureka client begins the registration process. It sends its instance information (name, host, port, status) to the Eureka server and changes its status from STARTING → UP.
3. Finally, Eureka responds with HTTP 204, which confirms that the service has been successfully registered and is now discoverable by other microservices.

### Web Service Registration

![Web Registration Log](docs/screenshots/web-registration.png)

```
2025-12-04T19:48:13.766+01:00  INFO 8203 --- [web-service] [foReplicator-%d] com.netflix.discovery.DiscoveryClient    : DiscoveryClient_WEB-SERVICE/localhost:web-service:4444 - registration status: 204
```

- **How the web service discovers the accounts service?**

1. When the Web Service starts, it registers itself in Eureka and then asks Eureka for the available instances of ACCOUNTS-SERVICE. Instead of using a fixed URL, it uses the service name; Eureka returns the service’s current host and port.
2. The Web Service then makes its REST calls using that information, so it can always find the Accounts Service even if its address changes.

---

## 3. Eureka Dashboard (Task 2)

![Eureka Dashboard](docs/screenshots/eureka-dashboard.png)

Describe what the Eureka dashboard shows:

- **Which services are registered?**

  The Eureka dashboard shows three registered services:
  - accounts-service
  - configserver
  - web-service

- **What information does Eureka track for each instance?**

  - Application name: the name of the registered service.
  - AMIs: image identifier (usually empty in local setups).
  - Availability Zones: the logical zone where the instance runs.
  - Status: the current state of the service (e.g., UP).
  - Instance address: the host and port where the instance is running.

---

## 4. Multiple Instances (Task 4)

![Multiple Instances](docs/screenshots/multiple-instances.png)

Answer the following questions:

- **What happens when you start a second instance of the accounts service?**

  When the second instance starts, it reads its configuration (port 2222) and registers itself with Eureka. Eureka now shows two instances of the ACCOUNTS-SERVICE: one on port 3333 and the new one on port 2222.

- **How does Eureka handle multiple instances?**

  Eureka tracks all instances separately. Each instance has its own metadata (port, status, host) and is listed individually in the dashboard. This allows clients to discover any available instance of the service.

- **How does client-side load balancing work with multiple instances?**

  When a client (like the Web Service) calls ACCOUNTS-SERVICE, it asks Eureka for the list of available instances. The client-side load balancer selects one instance from the list, distributing requests across all available instances to balance the load and improve resilience.

---

## 5. Service Failure Analysis (Task 5)

### Initial Failure

![Error Screenshot](docs/screenshots/failure-error.png)

- **What happens inmediately after stopping the accounts service on port 3333?**

  Immediately after stopping the Accounts Service on port 3333, both instances still appear as UP in Eureka. The dashboard does not detect the failure instantly. The log message changes slightly:

  - Before stopping: "RENEWALS ARE LESSER THAN THE THRESHOLD. THE SELF PRESERVATION MODE IS TURNED OFF. THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS."

  - After stopping: "THE SELF PRESERVATION MODE IS TURNED OFF. THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS."

  This indicates that Eureka does not immediately remove the failed instance.

### Eureka Instance Removal

![Instance Removal](docs/screenshots/instance-removal.png)

- **Explain how Eureka detects and removes the failed instance:**

  Eureka detects failed instances through heartbeats. Each service periodically sends a heartbeat to the Eureka server (`lease-renewal-interval-in-seconds: 5`). If an instance stops sending heartbeats, and the heartbeat absence exceeds `lease-expiration-duration-in-seconds` (10 seconds in this setup), Eureka marks the instance as DOWN and removes it from the registry.

  - **Mechanism:** Eureka relies on the heartbeat timeout. With self-preservation disabled (`enable-self-preservation: false`), failed instances are removed immediately after missing enough heartbeats.

  - **Detection time:** In this lab setup, removal typically occurs around 10 seconds after the instance stops sending heartbeats.
  
  ```
  eureka:
  instance:
    hostname: localhost
    lease-renewal-interval-in-seconds: 5
    lease-expiration-duration-in-seconds: 10
  ```
---

## 6. Service Recovery Analysis (Task 6)

![Recovery State](docs/screenshots/recovery.png)

Answer the following questions:

- **Why does the web service eventually recover?**

  After the failed Accounts Service instance (port 3333) is restarted, it registers again with Eureka. The Web Service queries Eureka for available instances, receives the updated list (including the recovered instance), and resumes normal communication.

- **How long did recovery take?**

  Recovery happens after the instance successfully sends heartbeats and is registered by Eureka. In this lab setup, it typically takes 5–10 seconds for Eureka to update its registry and for the Web Service to discover the recovered instance.

- **What role does client-side caching play in the recovery process?**

  The Web Service may cache the list of service instances to avoid querying Eureka on every request. When the cache expires or is refreshed, the Web Service retrieves the updated list, including the recovered instance, allowing it to route requests correctly. This ensures continued operation without constant Eureka calls but introduces a small delay in recognizing newly recovered instances.

---

## 7. Conclusions

In this lab, I learned the practical aspects of microservices architecture and how independent services can communicate and scale. Using Eureka for service discovery, I saw how services register themselves and how clients dynamically locate available instances without relying on hardcoded addresses. Working with multiple instances demonstrated system resilience and client-side load balancing, ensuring requests are evenly distributed and the system continues to function if one instance fails. The self-healing behavior of Eureka, combined with heartbeats, showed how failed instances are detected and removed, and how services recover automatically when restarted. Challenges included understanding heartbeat intervals, instance removal timing, and Eureka configuration nuances, which I solved by examining logs, configuration files, and the dashboard.

---

## 8. AI Disclosure

Yes, I used ChatGPT to help structure the report, summarize log outputs, and clarify explanations.

- I wrote all configurations, ran the services, and captured logs myself.

- I verified and interpreted the lab results personally to ensure a correct understanding of microservices behavior and Eureka’s service discovery mechanisms.

This report reflects my own comprehension of microservices patterns, Eureka registration and discovery, client-side load balancing, and system resilience concepts, with AI assisting mainly in drafting and organizing the explanations.

