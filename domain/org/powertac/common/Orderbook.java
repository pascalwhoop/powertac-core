/*
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.powertac.common;

import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.Instant;
import org.powertac.common.enumerations.ProductType;
import org.powertac.common.state.Domain;
import org.powertac.common.state.StateChange;
import org.powertac.common.xml.TimeslotConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * An orderbook instance captures a snapshot of the PowerTAC wholesale market's orderbook
 * (the un-cleared bids and asks remaining after the market is cleared). 
 * Each OrderbookEntry contained in the orderbook contains a limit price and
 * total un-cleared buy / sell quantity im mWh at that price.
 * Each time the market clears, one orderbook is created and sent to brokers for each
 * timeslot being traded during that clearing event.
 *
 * @author Daniel Schnurr
 * @version 1.2 , 05/02/2011
 */
@Domain
@XStreamAlias("orderbook")
public class Orderbook 
{
  @XStreamAsAttribute
  private long id = IdGenerator.createId();

  private Instant dateExecuted;

  /** the product this orderbook is generated for  */
  @XStreamAsAttribute
  private ProductType product = ProductType.Future;

  /** the timeslot this orderbook is generated for  */
  @XStreamConverter(TimeslotConverter.class)
  private Timeslot timeslot;

  /** last clearing price, expressed as a positive number - the price/mwh paid
   * to sellers. Null if the market did not clear.*/
  @XStreamAsAttribute
  private Double clearingPrice;


  /** sorted set of OrderbookOrder instances representing bids. Since bid
   * prices are negative, this is an ascending sort. */
  @XStreamImplicit(itemFieldName = "bid")
  private SortedSet<OrderbookOrder> bids = new TreeSet<OrderbookOrder>();

  /** sorted set of OrderbookOrders represenging asks ascending */
  @XStreamImplicit(itemFieldName = "ask")
  private SortedSet<OrderbookOrder> asks = new TreeSet<OrderbookOrder>();

  /**
   * Constructor with explicit product type. Useful if we must distinguish
   * more than one product type.
   */
  public Orderbook (Timeslot timeslot, ProductType product,
                    Double clearingPrice, Instant dateExecuted)
  {
    this(timeslot, clearingPrice, dateExecuted);
    this.product = product;
  }

  /**
   * Constructor with default product type.
   */
  public Orderbook (Timeslot timeslot, Double clearingPrice, Instant dateExecuted)
  {
    super();
    this.timeslot = timeslot;
    this.clearingPrice = clearingPrice;
    this.dateExecuted = dateExecuted;
  }
  
  public long getId ()
  {
    return id;
  }
  
  /**
   * Returns the positive price at which the market cleared. This is the price
   * paid to sellers, the negative of the price paid by buyers. Null if no
   * trades were cleared.
   */
  public Double getClearingPrice ()
  {
    return clearingPrice;
  }

  /**
   * The date when the market cleared.
   */
  public Instant getDateExecuted ()
  {
    return dateExecuted;
  }

  public ProductType getProduct ()
  {
    return product;
  }

  /**
   * The timeslot in which energy commitments represented by cleared trades
   * are due.
   */
  public Timeslot getTimeslot ()
  {
    return timeslot;
  }

  /**
   * The set of bids (positive energy quantities) that were submitted and
   * did not clear, ascending sort. Because bid prices are normally
   * negative, the first element is the highest (most negative) price that
   * did not clear. Null prices (market orders) sort ahead of non-null
   * prices (limit orders).
   */
  public SortedSet<OrderbookOrder> getBids ()
  {
    return bids;
  }
  
  @StateChange
  public Orderbook addBid (OrderbookOrder bid)
  {
    bids.add(bid);
    return this;
  }

  /**
   * The set of asks (negative energy quantities) that were submitted and
   * did not clear, ascending sort. Null prices (market orders) sort ahead 
   * of non-null prices (limit orders).
   */
  public SortedSet<OrderbookOrder> getAsks ()
  {
    return asks;
  }
  
  @StateChange
  public Orderbook addAsk (OrderbookOrder ask)
  {
    asks.add(ask);
    return this;
  }
}