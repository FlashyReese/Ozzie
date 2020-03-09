/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.wilsonhu.ozzie;

public class Application {

    public static void main(String[] args) throws Exception {
        Ozzie ozzie = new Ozzie(args);
        ozzie.start();
        //Todo: Add back fucking quotes, it's not Ozzie without her quotes qq, Laura Ily pls love me - Update: quotes added but find a way to schedule changes look at TodoList xd
        //Todo: Start Documenting this shit
        //Todo: Get started on Vaadin WebApp(Maybe Laravel if I do implement what's below xdxd) so I can fully deploy this shit
        //Todo: add a shutdown hook and fucking link those plugins to disable every single one of them xd
        //Todo: ScheduledExecutorService
    }
}
