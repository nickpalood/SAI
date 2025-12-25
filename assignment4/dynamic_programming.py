#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dynamic Programming 
Practical for course 'Symbolic AI'
2020, Leiden University, The Netherlands
By Thomas Moerland
"""

import numpy as np
from world import World

class Dynamic_Programming:

    def __init__(self):
        self.V_s = None
        self.Q_sa = None
        
    def value_iteration(self,env,gamma = 1.0, theta=0.001):
        ''' Executes value iteration on env. 
        gamma is the discount factor of the MDP
        theta is the acceptance threshold for convergence '''

        print("Starting Value Iteration (VI)")
        V_s = np.zeros(env.n_states)
        
        iteration = 0
        while True:
            delta = 0
            iteration += 1
            
            for s in env.states:
                v = V_s[s]
                
                action_values = np.zeros(env.n_actions)
                for a_idx, action in enumerate(env.actions):
                    s_prime, r = env.transition_function(s, action)
                    action_values[a_idx] = r + gamma * V_s[s_prime]
                
                V_s[s] = np.max(action_values)
                delta = max(delta, np.abs(v - V_s[s]))
            
            print(f"Iteration {iteration}: Delta = {delta}")
            
            if delta < theta:
                print(f"Value Iteration converged after {iteration} iterations")
                break
        
        self.V_s = V_s
        return

    def Q_value_iteration(self,env,gamma = 1.0, theta=0.001):
        ''' Executes Q-value iteration on env. 
        gamma is the discount factor of the MDP
        theta is the acceptance threshold for convergence '''

        print("Starting Q-value Iteration (QI)")
        Q_sa = np.zeros([env.n_states,env.n_actions])

        iteration = 0
        while True:
            delta = 0
            iteration += 1
            
            for s in env.states:
                for a_idx, action in enumerate(env.actions):
                    q = Q_sa[s, a_idx]
                    
                    s_prime, r = env.transition_function(s, action)
                    Q_sa[s, a_idx] = r + gamma * np.max(Q_sa[s_prime, :])
                    
                    delta = max(delta, np.abs(q - Q_sa[s, a_idx]))
            
            print(f"Iteration {iteration}: Delta = {delta}")
            
            if delta < theta:
                print(f"Q-value Iteration converged after {iteration} iterations")
                break

        self.Q_sa = Q_sa
        return
                
    def execute_policy(self,env,table='V'):
        env.reset_agent()
        print("Start executing. Current map:") 
        env.print_map()
        while not env.terminal:
            current_state = env.get_current_state()
            available_actions = env.actions
            
            if table == 'V' and self.V_s is not None:
                action_values = np.zeros(env.n_actions)
                for a_idx, action in enumerate(env.actions):
                    s_prime, r = env.transition_function(current_state, action)
                    action_values[a_idx] = r + 1.0 * self.V_s[s_prime]
                
                greedy_action = env.actions[np.argmax(action_values)]
            
            elif table == 'Q' and self.Q_sa is not None:
                action_values = self.Q_sa[current_state, :]
                greedy_action = env.actions[np.argmax(action_values)]
                
            else:
                print("No optimal value table was detected. Only manual execution possible.")
                greedy_action = None

            while True:
                if greedy_action is not None:
                    print('Greedy action= {}'.format(greedy_action))    
                    your_choice = input('Choose an action by typing it in full, then hit enter. Just hit enter to execute the greedy action:')
                else:
                    your_choice = input('Choose an action by typing it in full, then hit enter. Available are {}'.format(env.actions))
                    
                if your_choice == "" and greedy_action is not None:
                    executed_action = greedy_action
                    env.act(executed_action)
                    break
                else:
                    try:
                        executed_action = your_choice
                        env.act(executed_action)
                        break
                    except:
                        print('{} is not a valid action. Available actions are {}. Try again'.format(your_choice,env.actions))
            print("Executed action: {}".format(executed_action))
            print("--------------------------------------\nNew map:")
            env.print_map()
        print("Found the goal! Exiting \n ...................................................................... ")
    

def get_greedy_index(action_values):
    ''' Own variant of np.argmax, since np.argmax only returns the first occurence of the max. 
    Optional to uses '''
    return np.where(action_values == np.max(action_values))
    
if __name__ == '__main__':
    env = World('prison.txt') 
    DP = Dynamic_Programming()

    input('Press enter to run value iteration')
    optimal_V_s = DP.value_iteration(env)
    input('Press enter to start execution of optimal policy according to V')
    DP.execute_policy(env, table='V')
    
    input('Press enter to run Q-value iteration')
    optimal_Q_sa = DP.Q_value_iteration(env)
    input('Press enter to start execution of optimal policy according to Q')
    DP.execute_policy(env, table='Q')