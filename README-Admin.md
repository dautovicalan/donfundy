# DonFundy - Admin Guide

## Overview

As an administrator, you have full access to manage campaigns, view all donations, and perform bulk operations.

## Getting Started

1. Log in with your admin credentials at `/login`
2. Access the Admin Dashboard from the navigation bar (yellow "Admin" link)

## Features

### Admin Dashboard (`/admin`)

- **Statistics Overview**: View total campaigns, donations, donors, and amount raised
- **Bulk Donation Upload**: Import multiple donations via CSV file
- **Recent Donations**: Quick view of the latest 10 donations

### Campaign Management

| Action | Description |
|--------|-------------|
| Create | Add new fundraising campaigns |
| Edit | Modify campaign details and status |
| Delete | Remove campaigns from the system |
| View | See all campaign details and donations |

### Bulk Donation Upload

Upload a CSV file to import multiple donations at once.

**CSV Format:**
```
campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
1,100.50,john@example.com,John,Doe,CARD,Thank you
1,50.00,anonymous,,,BANK_TRANSFER,
```

**Fields:**
- `campaignId` - ID of the target campaign (required)
- `amount` - Donation amount (required, must be positive)
- `donorEmail` - Donor's email or "anonymous" (required)
- `donorFirstName` - Donor's first name (optional for anonymous)
- `donorLastName` - Donor's last name (optional for anonymous)
- `paymentMethod` - CARD, BANK_TRANSFER, or PAYPAL (required)
- `message` - Optional message

### Campaign Statuses

- **PENDING** - Campaign awaiting activation
- **ACTIVE** - Campaign accepting donations
- **COMPLETED** - Goal reached or manually completed
- **CANCELLED** - Campaign cancelled

## API Documentation

Access Swagger UI at: `http://localhost:8085/api/v1/swagger-ui/index.html`

Use the "Authorize" button to add your JWT token for authenticated requests.
