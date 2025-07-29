# Deploying the Calorie Counter Application to AWS EC2

This guide provides step-by-step instructions for deploying the Calorie Counter application to an Amazon EC2 instance using Docker and Docker Compose.

## Prerequisites

- An AWS Account.

- Your project code pushed to a Git repository (e.g., GitHub, GitLab).

- The following API keys, which you will configure on the server:
    - `CALORIE_NINJAS_API_KEY`
    - `GEMINI_API_KEY`
    - `TELEGRAM_BOT_TOKEN`

## Step 1: Launch an EC2 Instance

1.  Navigate to the **EC2 Dashboard** in the AWS Management Console.

2.  Click **Launch instance**.

3.  Configure the instance with the following settings:
    - **Name**: `calorie-counter-server` (or any name you prefer).

    - **Application and OS Images (AMI)**: Select `Amazon Linux 2023 AMI`. It's free-tier eligible and comes with modern tools.

    - **Instance type**: Choose `t2.micro` or `t3.micro` (both are free-tier eligible).

    - **Key pair (login)**: Create a new key pair or select an existing one. Download the `.pem` file and store it securely. You'll need it to connect to your instance.

    - **Network settings**:
        - Click `Edit`.
        - Under **Firewall (security groups)**, select **Create security group**.
        - Add the following inbound security group rules:
            - **Rule 1**:
                - Type: `SSH`
                - Protocol: `TCP`
                - Port range: `22`
                - Source type: `My IP` (This enhances security by restricting SSH access to your current IP address).
            - **Rule 2**:
                - Type: `Custom TCP`
                - Protocol: `TCP`
                - Port range: `8080`
                - Source type: `Anywhere` (`0.0.0.0/0`) to allow public access to your application.

    - **Configure storage**: The default 8 GiB is sufficient for this project.

4.  Review the configuration and click **Launch instance**.

## Step 2: Connect to Your EC2 Instance

1.  From the EC2 instances list, select your new instance and copy its **Public IPv4 address**.

2.  Open a terminal on your local machine.

3.  Use the `ssh` command to connect. Replace `your-key.pem` with the path to your key file and `your-ec2-public-ip` with your instance's public IP. The default user for Amazon Linux is `ec2-user`.

    ````bash
    # Set the correct permissions for your key file
    chmod 400 your-key.pem

    # Connect to the instance
    ssh -i "your-key.pem" ec2-user@your-ec2-public-ip
    ````

## Step 3: Set Up the EC2 Environment

Once connected via SSH, run the following commands to install the necessary software.

1.  **Update system packages**:

    ````bash
    sudo yum update -y
    ````

2.  **Install Git and Docker**:

    ````bash
    sudo yum install -y git docker
    ````

3.  **Start and enable the Docker service**:

    ````bash
    sudo systemctl start docker
    sudo systemctl enable docker
    ````

4.  **Add `ec2-user` to the `docker` group** to run Docker commands without `sudo`.

    ````bash
    sudo usermod -a -G docker ec2-user
    ````

5.  **Log out and log back in** for the group permissions to take effect.

    ````bash
    exit
    ````
    Reconnect using the same `ssh` command as in Step 2.

6.  **Verify the installation**. The `docker compose` command is now the standard and should be available.

    ````bash
    docker --version
    docker compose version
    ````

## Step 4: Deploy the Application

1.  **Clone your project repository**. Replace `your-repository-url` with the actual URL.

    ````bash
    git clone your-repository-url calorie-counter
    cd calorie-counter
    ````

2.  **Create the environment file**. The `docker-compose.yaml` is configured to read variables from a `.env` file.

    ````bash
    nano .env
    ````

3.  Add your secret keys to the `.env` file. Replace the placeholder values with your actual keys.

    ````
    # .env file for Docker Compose
    CALORIE_NINJAS_API_KEY=your_calorie_ninjas_api_key
    GEMINI_API_KEY=your_gemini_api_key
    TELEGRAM_BOT_TOKEN=your_telegram_bot_token
    ````

    Save and exit `nano` (press `Ctrl+X`, then `Y`, then `Enter`).

4.  **Build and run the application** using Docker Compose.

    ````bash
    docker compose up --build -d
    ````
    - `--build`: Builds the Docker image from the `Dockerfile` before starting the service.
    - `-d`: Runs the container in detached mode (in the background).

## Step 5: Verify the Deployment

1.  **Check running containers**:

    ````bash
    docker compose ps
    ````
    You should see the `calorie-counter-app` container with a status of `Up` or `running`.

2.  **View application logs** to ensure it started correctly:

    ````bash
    docker compose logs -f
    ````
    Look for the Spring Boot startup logs. Press `Ctrl+C` to exit.

3.  **Test the health endpoint from the EC2 instance**:

    ````bash
    curl http://localhost:8080/health
    ````
    The expected response is: `{"status":"UP"}`.

4.  **Test from your local browser**:
    Open a web browser and navigate to `http://your-ec2-public-ip:8080/health`. You should see the same `{"status":"UP"}` response.

Your application is now successfully deployed on AWS EC2!

## Next Steps (Optional but Recommended)

-   **Domain Name**: Use Amazon Route 53 to point a custom domain to your EC2 instance.

-   **Enable HTTPS**: Set up a reverse proxy like Nginx in front of your application to handle SSL/TLS encryption. You can get free certificates from Let's Encrypt.

-   **CI/CD Pipeline**: Automate deployments using GitHub Actions, GitLab CI/CD, or AWS CodePipeline to automatically build and deploy new changes.

-   **Secrets Management**: For enhanced security in a production environment, consider using AWS Secrets Manager or AWS Systems Manager Parameter Store instead of a plain `.env` file.

