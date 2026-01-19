# DonFundy - User Guide

## Overview

DonFundy is a donation platform that allows you to browse fundraising campaigns and make donations to causes you care about.

## Getting Started

1. Register for an account at `/register`
2. Log in with your credentials at `/login`
3. Browse campaigns and start donating

## Features

### Browse Campaigns (`/campaigns`)

- View all active fundraising campaigns
- See campaign progress (amount raised vs goal)
- Filter campaigns by status
- Click on any campaign for more details

### Campaign Details (`/campaigns/:id`)

- Full campaign description
- Real-time progress bar
- List of recent donations
- Donate button (for active campaigns)

### Make a Donation (`/campaigns/:id/donate`)

1. Navigate to a campaign you want to support
2. Click "Donate Now"
3. Enter donation amount
4. Select payment method:
   - Credit/Debit Card
   - Bank Transfer
   - PayPal
5. Add an optional message
6. Submit your donation

### Your Profile

- View your donor profile
- See your donation history

## Payment Methods

| Method | Description |
|--------|-------------|
| CARD | Credit or Debit Card |
| BANK_TRANSFER | Direct Bank Transfer |
| PAYPAL | PayPal Payment |

## Campaign Statuses

- **ACTIVE** - Accepting donations
- **COMPLETED** - Goal reached
- **PENDING** - Not yet active
- **CANCELLED** - No longer accepting donations

## Language Support

Toggle between English and Spanish using the language switcher in the navigation bar.

## Need Help?

Contact support at support@donfundy.com
